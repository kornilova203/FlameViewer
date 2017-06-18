const CANVAS_WIDTH = 700;
const CANVAS_HEIGHT = 400;
const LAYER_HEIGHT = 15;
const COLORS = ["#18A3FA", "#0887d7"];

class Drawer {
    constructor(tree) {
        this.tree = tree;
        this.stage = null;
        this.duration = this.tree.getDuration();
        this.startTime = this.tree.getStarttime();
    }

    drawTree() {
        this._createSection();
        this.stage = new createjs.Stage("canvas-" + this.tree.getThreadid());

        this._drawRecursively(this.tree, 0);

        this.stage.update();
    };

    _drawRecursively(call, depth) {
        const childCalls = call.getCallsList();
        if (childCalls.length === 0) {
            return;
        }
        for (let i = 0; i < childCalls.length; i++) {
            this._drawCall(childCalls[i], depth, i % 2);
            this._drawRecursively(childCalls[i], depth + 1);
        }
    }

    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @private
     */
    static _flipY(y) {
        return CANVAS_HEIGHT - y - LAYER_HEIGHT;
    };

    _createSection() {
        $("main").append(templates.tree.getSectionForThread(
            {
                threadId: this.tree.getThreadid(),
                canvasHeight: 400
            }
        ).content);
    };

    _drawCall(call, depth, colorId) {
        const shape = new createjs.Shape();
        shape.graphics
            .beginFill(COLORS[colorId])
            .drawRect(0, 0, CANVAS_WIDTH, LAYER_HEIGHT);
        const offsetX = ((call.getStarttime() - this.startTime) / this.duration) * CANVAS_WIDTH;
        const scaleX = call.getDuration() / this.duration;
        shape.setTransform(offsetX, Drawer._flipY(depth * 16), scaleX);
        console.log(`draw: ${depth}\t${scaleX}\t${call.getEnter().getMethodname()}`);
        this.stage.addChild(shape);
    }
}
