export default interface SwipeCallbackEvent {
    /**
     * The original event received from the browser.
     */
    originalEvent: MouseEvent|TouchEvent,
    /**
     * The left position of the element at the moment the swipe was started.
     */
    originalLeft: number,
    /**
     * The horizontal delta the swipe has already moved (negative values mean to the left of the original left position).
     */
    deltaX: number,
    /**
     * The current left position of the element.
     */
    newLeft: number,
    /**
     * -1 if the move is to the left, 1 if the move is to the right, 0 or -0 if it is not moved yet
     */
    direction: number
}
