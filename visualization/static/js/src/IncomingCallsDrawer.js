class IncomingCallsDrawer extends AccumulativeTreeDrawer {

    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @protected
     */
    flipY(y) {
        return y + 40;
    }
}