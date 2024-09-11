package org.jetbrains.jewel.foundation.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.FocusedBoundsObserverNode
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.ContentInViewNode
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.MouseWheelScrollNode
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableContainerNode
import androidx.compose.foundation.gestures.ScrollableNestedScrollConnection
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.ScrollingLogic
import androidx.compose.foundation.gestures.platformDefaultFlingBehavior
import androidx.compose.foundation.gestures.platformScrollConfig
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewResponderNode
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.FocusPropertiesModifierNode
import androidx.compose.ui.focus.FocusTargetModifierNode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.UserInput
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.modifier.shaded.DragGestureNode
import org.jetbrains.jewel.foundation.modifier.shaded.detectDragGestures
import kotlin.math.sign

public fun Modifier.freeScrollable(
    state: FreeScrollableState,
    overscrollEffect: OverscrollEffect?,
    enabled: Boolean = true,
    reverseDirection: Boolean = false,
    flingBehavior: FreeScrollFlingBehavior? = null,
    interactionSource: MutableInteractionSource? = null,
    bringIntoViewSpec: BringIntoViewSpec? = null,
): Modifier = this then FreeScrollableElement(
    state,
    overscrollEffect,
    enabled,
    reverseDirection,
    flingBehavior,
    interactionSource,
    bringIntoViewSpec
)

public interface FreeScrollableState {
    public val horizontalState: ScrollableState

    public val verticalState: ScrollableState

    public suspend fun scroll(
        scrollPriority: MutatePriority = MutatePriority.Default,
        block: suspend FreeScrollScope.() -> Unit,
    )

    public fun dispatchRawDelta(delta: Offset): Offset

    public val isScrollInProgress: Boolean
}

public interface FreeScrollScope {
    public fun scrollBy(pixels: Offset): Offset
}

private class FreeScrollableElement(
    val state: FreeScrollableState,
    val overscrollEffect: OverscrollEffect?,
    val enabled: Boolean,
    val reverseDirection: Boolean,
    val flingBehavior: FreeScrollFlingBehavior?,
    val interactionSource: MutableInteractionSource?,
    val bringIntoViewSpec: BringIntoViewSpec?,
) : ModifierNodeElement<FreeScrollableNode>() {
    override fun create(): FreeScrollableNode {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        TODO("Not yet implemented")
    }

    override fun update(node: FreeScrollableNode) {
        TODO("Not yet implemented")
    }
}

public interface FreeScrollFlingBehavior {
    public suspend fun FreeScrollScope.performFling(initialVelocity: Velocity): Velocity
}

private class FreeScrollableNode(
    state: FreeScrollableState,
    private var overscrollEffect: OverscrollEffect?,
    private var flingBehavior: FreeScrollFlingBehavior?,
    enabled: Boolean,
    reverseDirection: Boolean,
    interactionSource: MutableInteractionSource?,
    bringIntoViewSpec: BringIntoViewSpec?,
) : FreeDragGestureNode(enabled, interactionSource),
    ObserverModifierNode, CompositionLocalConsumerModifierNode,
    FocusPropertiesModifierNode, KeyInputModifierNode, SemanticsModifierNode {

    override val shouldAutoInvalidate: Boolean = false

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    private val scrollableContainerNode =
        delegate(ScrollableContainerNode(enabled))

    private val defaultFlingBehavior = platformDefaultFlingBehavior()

    private val scrollingLogic = FreeScrollingLogic(
        scrollableState = state,
        overscrollEffect = overscrollEffect,
        reverseDirection = reverseDirection,
        flingBehavior = flingBehavior ?: defaultFlingBehavior,
        nestedScrollDispatcher = nestedScrollDispatcher,
    )

    private val nestedScrollConnection =
        FreeScrollableNestedScrollConnection(enabled = enabled, scrollingLogic = scrollingLogic)

    private val contentInViewNode =
        delegate(
            ContentInViewNode(
                orientation,
                scrollingLogic,
                reverseDirection,
                bringIntoViewSpec
            )
        )


    init {
        /**
         * Nested scrolling
         */
        delegate(nestedScrollModifierNode(nestedScrollConnection, nestedScrollDispatcher))

        /**
         * Focus scrolling
         */
        delegate(FocusTargetModifierNode())
        delegate(BringIntoViewResponderNode(contentInViewNode))
        delegate(FocusedBoundsObserverNode { contentInViewNode.onFocusBoundsChanged(it) })
    }

    override suspend fun drag(
        forEachDelta: suspend ((dragDelta: Offset) -> Unit) -> Unit
    ) {
        with(scrollingLogic) {
            scroll(scrollPriority = MutatePriority.UserInput) {
                forEachDelta {
                    scrollByWithOverscroll(
                        it,
                        source = UserInput
                    )
                }
            }
        }
    }

    override fun onDragStarted(startedPosition: Offset) {}

    override fun onDragStopped(velocity: Velocity) {
        nestedScrollDispatcher.coroutineScope.launch {
            scrollingLogic.onScrollStopped(velocity, isMouseWheel = false)
        }
    }

    override fun startDragImmediately(): Boolean {
        return scrollingLogic.shouldScrollImmediately()
    }

    private val onWheelScrollStopped: suspend (velocity: Velocity) -> Unit = { velocity ->
        nestedScrollDispatcher.coroutineScope.launch {
            scrollingLogic.onScrollStopped(velocity, isMouseWheel = true)
        }
    }

    val mouseWheelScrollNode = delegate(
        MouseWheelScrollNode(
            scrollingLogic = scrollingLogic,
            onScrollStopped = onWheelScrollStopped,
            enabled = enabled,
        )
    )

    override fun onAttach() {
        updateDefaultFlingBehavior()
        scrollConfig = platformScrollConfig()
    }

    override fun onObservedReadsChanged() {
        updateDefaultFlingBehavior()
    }

    private fun updateDefaultFlingBehavior() {
        // monitor change in Density
        observeReads {
            val density = currentValueOf(LocalDensity)
            defaultFlingBehavior.updateDensity(density)
        }
    }

    override fun applyFocusProperties(focusProperties: FocusProperties) {
        focusProperties.canFocus = false
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return if (enabled &&
            (event.key == Key.PageDown || event.key == Key.PageUp) &&
            (event.type == KeyEventType.KeyDown) &&
            (!event.isCtrlPressed)
        ) {
            val scrollAmount: Offset = if (!event.isShiftPressed) {
                val viewportHeight = contentInViewNode.viewportSize.height

                val yAmount = if (event.key == Key.PageUp) {
                    viewportHeight.toFloat()
                } else {
                    -viewportHeight.toFloat()
                }

                Offset(0f, yAmount)
            } else {
                val viewportWidth = contentInViewNode.viewportSize.width

                val xAmount = if (event.key == Key.PageUp) {
                    viewportWidth.toFloat()
                } else {
                    -viewportWidth.toFloat()
                }

                Offset(xAmount, 0f)
            }

            // A coroutine is launched for every individual scroll event in the
            // larger scroll gesture. If we see degradation in the future (that is,
            // a fast scroll gesture on a slow device causes UI jank [not seen up to
            // this point), we can switch to a more efficient solution where we
            // lazily launch one coroutine (with the first event) and use a Channel
            // to communicate the scroll amount to the UI thread.
            coroutineScope.launch {
                scrollingLogic.scroll(scrollPriority = MutatePriority.UserInput) {
                    scrollBy(
                        offset = scrollAmount,
                        source = UserInput
                    )
                }
            }
            true
        } else {
            false
        }
    }

    override fun onPreKeyEvent(event: KeyEvent): Boolean = false

    override fun SemanticsPropertyReceiver.applySemantics() {
        TODO("Not yet implemented")
    }
}

private abstract class FreeDragGestureNode(
    enabled: Boolean,
    interactionSource: MutableInteractionSource?,
) : DelegatingNode(),
    PointerInputModifierNode,
    CompositionLocalConsumerModifierNode {

    protected var enabled = enabled
        private set
    protected var interactionSource = interactionSource
        private set
    private fun canDrag(change: PointerInputChange): Boolean = change.type != PointerType.Mouse
    private var channel: Channel<DragEvent>? = null
    private var dragInteraction: DragInteraction.Start? = null
    private var isListeningForEvents = false
    private var pointerInputNode: SuspendingPointerInputModifierNode? = null

    /**
     * Responsible for the dragging behavior between the start and the end of the drag. It
     * continually invokes `forEachDelta` to process incoming events. In return, `forEachDelta`
     * calls `dragBy` method to process each individual delta.
     */
    abstract suspend fun drag(forEachDelta: suspend ((dragDelta: Offset) -> Unit) -> Unit)

    /**
     * Passes the action needed when a drag starts. This gives the ability to pass the desired
     * behavior from other nodes implementing AbstractDraggableNode
     */
    abstract fun onDragStarted(startedPosition: Offset)

    /**
     * Passes the action needed when a drag stops. This gives the ability to pass the desired
     * behavior from other nodes implementing AbstractDraggableNode
     */
    abstract fun onDragStopped(velocity: Velocity)

    /**
     * If touch slop recognition should be skipped. If this is true, this node will start
     * recognizing drag events immediately without waiting for touch slop.
     */
    abstract fun startDragImmediately(): Boolean

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (enabled && pointerInputNode == null) {
            pointerInputNode = delegate(initializePointerInputNode())
        }
        pointerInputNode?.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() {
        pointerInputNode?.onCancelPointerInput()
    }

    private fun initializePointerInputNode(): SuspendingPointerInputModifierNode {
        return SuspendingPointerInputModifierNode {
            // re-create tracker when pointer input block restarts. This lazily creates the tracker
            // only when it is need.
            val velocityTracker = VelocityTracker()
            val onDragStart: (change: PointerInputChange, initialDelta: Offset) -> Unit =
                { startEvent, initialDelta ->
                    if (canDrag(startEvent)) {
                        if (!isListeningForEvents) {
                            if (channel == null) {
                                channel = Channel(capacity = Channel.UNLIMITED)
                            }
                            startListeningForEvents()
                        }
                        val overSlopOffset = initialDelta
                        val xSign = sign(startEvent.position.x)
                        val ySign = sign(startEvent.position.y)
                        val adjustedStart = startEvent.position -
                            Offset(overSlopOffset.x * xSign, overSlopOffset.y * ySign)

                        channel?.trySend(DragEvent.DragStarted(adjustedStart))
                    }
                }

            val onDragEnd: (change: PointerInputChange) -> Unit = { upEvent ->
                velocityTracker.addPointerInputChange(upEvent)
                val maximumVelocity = currentValueOf(LocalViewConfiguration)
                    .maximumFlingVelocity
                val velocity = velocityTracker.calculateVelocity(
                    Velocity(maximumVelocity, maximumVelocity)
                )
                velocityTracker.resetTracking()
                channel?.trySend(DragEvent.DragStopped(velocity))
            }

            val onDragCancel: () -> Unit = {
                channel?.trySend(DragEvent.DragCancelled)
            }

            val shouldAwaitTouchSlop: () -> Boolean = {
                !startDragImmediately()
            }

            val onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit =
                { change, delta ->
                    velocityTracker.addPointerInputChange(change)
                    channel?.trySend(DragEvent.DragDelta(delta))
                }

            coroutineScope {
                try {
                    detectDragGestures(
                        orientationLock = null,
                        onDragStart = onDragStart,
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                        shouldAwaitTouchSlop = shouldAwaitTouchSlop,
                        onDrag = onDrag
                    )
                } catch (cancellation: CancellationException) {
                    channel?.trySend(DragEvent.DragCancelled)
                    if (!isActive) throw cancellation
                }
            }
        }
    }

    private fun startListeningForEvents() {
        isListeningForEvents = true

        /**
         * To preserve the original behavior we had (before the Modifier.Node migration) we need to
         * scope the DragStopped and DragCancel methods to the node's coroutine scope instead of using
         * the one provided by the pointer input modifier, this is to ensure that even when the pointer
         * input scope is reset we will continue any coroutine scope scope that we started from these
         * methods while the pointer input scope was active.
         */
        coroutineScope.launch {
            while (isActive) {
                var event = channel?.receive()
                if (event !is DragEvent.DragStarted) continue
                processDragStart(event)
                try {
                    drag { processDelta ->
                        while (event !is DragEvent.DragStopped && event !is DragEvent.DragCancelled) {
                            (event as? DragEvent.DragDelta)?.delta?.let(processDelta)
                            event = channel?.receive()
                        }
                    }
                    if (event is DragEvent.DragStopped) {
                        processDragStop(event as DragEvent.DragStopped)
                    } else if (event is DragEvent.DragCancelled) {
                        processDragCancel()
                    }
                } catch (c: CancellationException) {
                    processDragCancel()
                }
            }
        }
    }

    private suspend fun processDragStart(event: DragEvent.DragStarted) {
        dragInteraction?.let { oldInteraction ->
            interactionSource?.emit(DragInteraction.Cancel(oldInteraction))
        }
        val interaction = DragInteraction.Start()
        interactionSource?.emit(interaction)
        dragInteraction = interaction
        onDragStarted(event.startPoint)
    }

    private suspend fun processDragStop(event: DragEvent.DragStopped) {
        dragInteraction?.let { interaction ->
            interactionSource?.emit(DragInteraction.Stop(interaction))
            dragInteraction = null
        }
        onDragStopped(event.velocity)
    }

    private suspend fun processDragCancel() {
        dragInteraction?.let { interaction ->
            interactionSource?.emit(DragInteraction.Cancel(interaction))
            dragInteraction = null
        }
        onDragStopped(Velocity.Zero)
    }

    fun disposeInteractionSource() {
        dragInteraction?.let { interaction ->
            interactionSource?.tryEmit(DragInteraction.Cancel(interaction))
            dragInteraction = null
        }
    }
}

private val NoOpFreeScrollScope: FreeScrollScope = object : FreeScrollScope {
    override fun scrollBy(pixels: Offset): Offset = pixels
}

@OptIn(ExperimentalFoundationApi::class)
internal class FreeScrollingLogic(
    var scrollableState: FreeScrollableState,
    private var overscrollEffect: OverscrollEffect?,
    private var flingBehavior: FreeScrollFlingBehavior,
    private var reverseDirection: Boolean,
    private var nestedScrollDispatcher: NestedScrollDispatcher,
) {
    fun Velocity.reverseIfNeeded(): Velocity = if (reverseDirection) this * -1f else this

    fun Offset.reverseIfNeeded(): Offset = if (reverseDirection) this * -1f else this

    private var latestScrollSource = UserInput

    private var outerStateScope = NoOpFreeScrollScope

    private val nestedScrollScope = object : NestedScrollScope {
        override fun scrollBy(offset: Offset, source: NestedScrollSource): Offset {
            return with(outerStateScope) {
                performScroll(offset, source)
            }
        }

        override fun scrollByWithOverscroll(offset: Offset, source: NestedScrollSource): Offset {
            latestScrollSource = source
            val overscroll = overscrollEffect
            return if (overscroll != null && false) {
                overscroll.applyToScroll(offset, latestScrollSource, performScrollForOverscroll)
            } else {
                with(outerStateScope) {
                    performScroll(offset, source)
                }
            }
        }
    }

    private val performScrollForOverscroll: (Offset) -> Offset = { delta ->
        with(outerStateScope) {
            performScroll(delta, latestScrollSource)
        }
    }

    private fun FreeScrollScope.performScroll(delta: Offset, source: NestedScrollSource): Offset {
        val consumedByPreScroll =
            nestedScrollDispatcher.dispatchPreScroll(delta, source)

        val scrollAvailableAfterPreScroll = delta - consumedByPreScroll

        val deltaForSelfScroll =
            scrollAvailableAfterPreScroll.reverseIfNeeded()

        // Consume on a single axis.
        val consumedBySelfScroll =
            scrollBy(deltaForSelfScroll).reverseIfNeeded()

        val deltaAvailableAfterScroll = scrollAvailableAfterPreScroll - consumedBySelfScroll
        val consumedByPostScroll = nestedScrollDispatcher.dispatchPostScroll(
            consumedBySelfScroll,
            deltaAvailableAfterScroll,
            source
        )
        return consumedByPreScroll + consumedBySelfScroll + consumedByPostScroll
    }

    fun performRawScroll(scroll: Offset): Offset {
        return if (scrollableState.isScrollInProgress) {
            Offset.Zero
        } else {
            dispatchRawDelta(scroll)
        }
    }

    fun dispatchRawDelta(scroll: Offset): Offset {
        return scrollableState.dispatchRawDelta(scroll.reverseIfNeeded())
            .reverseIfNeeded()
    }

    suspend fun onScrollStopped(
        initialVelocity: Velocity,
        isMouseWheel: Boolean,
    ) {
        if (isMouseWheel) {
            return
        }
        val availableVelocity = initialVelocity

        scroll {
            val performFling: suspend (Velocity) -> Velocity = { velocity ->
                val preConsumedByParent = nestedScrollDispatcher
                    .dispatchPreFling(velocity)
                val available = velocity - preConsumedByParent
                val velocityLeft = doFlingAnimation(available)
                val consumedPost =
                    nestedScrollDispatcher.dispatchPostFling(
                        (available - velocityLeft),
                        velocityLeft
                    )
                val totalLeft = velocityLeft - consumedPost
                velocity - totalLeft
            }

            val overscroll = overscrollEffect
            if (overscroll != null && false) {
                overscroll.applyToFling(availableVelocity, performFling)
            } else {
                performFling(availableVelocity)
            }
        }
    }

    suspend fun NestedScrollScope.doFlingAnimation(available: Velocity): Velocity {
        var result: Velocity = available

        val nestedScrollScope = this
        val reverseScope = object : FreeScrollScope {
            override fun scrollBy(pixels: Offset): Offset {
                return nestedScrollScope.scrollByWithOverscroll(
                    offset = pixels.reverseIfNeeded(),
                    source = SideEffect
                ).reverseIfNeeded()
            }
        }
        with(reverseScope) {
            with(flingBehavior) {
                result = performFling(available.reverseIfNeeded()).reverseIfNeeded()
            }
        }
        return result
    }

    fun shouldScrollImmediately(): Boolean {
        return scrollableState.isScrollInProgress ||
            overscrollEffect?.isInProgress ?: false
    }

    /**
     * Opens a scrolling session with nested scrolling and overscroll support.
     */
    suspend fun scroll(
        scrollPriority: MutatePriority = MutatePriority.Default,
        block: suspend NestedScrollScope.() -> Unit,
    ) {
        scrollableState.scroll(scrollPriority) {
            outerStateScope = this
            block.invoke(nestedScrollScope)
        }
    }

    /**
     * @return true if the pointer input should be reset
     */
    fun update(
        scrollableState: FreeScrollableState,
        overscrollEffect: OverscrollEffect?,
        reverseDirection: Boolean,
        flingBehavior: FreeScrollFlingBehavior,
        nestedScrollDispatcher: NestedScrollDispatcher,
    ): Boolean {
        var resetPointerInputHandling = false
        if (this.scrollableState != scrollableState) {
            this.scrollableState = scrollableState
            resetPointerInputHandling = true
        }
        this.overscrollEffect = overscrollEffect
        if (this.reverseDirection != reverseDirection) {
            this.reverseDirection = reverseDirection
            resetPointerInputHandling = true
        }
        this.flingBehavior = flingBehavior
        this.nestedScrollDispatcher = nestedScrollDispatcher
        return resetPointerInputHandling
    }
}

internal interface NestedScrollScope {
    fun scrollBy(
        offset: Offset,
        source: NestedScrollSource,
    ): Offset

    fun scrollByWithOverscroll(
        offset: Offset,
        source: NestedScrollSource,
    ): Offset
}

private class FreeScrollableNestedScrollConnection(
    val scrollingLogic: FreeScrollingLogic,
    var enabled: Boolean
) : NestedScrollConnection {

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = if (enabled) {
        scrollingLogic.performRawScroll(available)
    } else {
        Offset.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        return if (enabled) {
            var velocityLeft: Velocity = available
            with(scrollingLogic) {
                scroll {
                    velocityLeft = doFlingAnimation(available)
                }
            }
            available - velocityLeft
        } else {
            Velocity.Zero
        }
    }
}

internal sealed class DragEvent {
    class DragStarted(val startPoint: Offset) : DragEvent()
    class DragStopped(val velocity: Velocity) : DragEvent()
    object DragCancelled : DragEvent()
    class DragDelta(val delta: Offset) : DragEvent()
}
