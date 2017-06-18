const CANVAS_WIDTH = 700;
const CANVAS_HEIGHT = 400;
const LAYER_HEIGHT = 15;
const RECT_GRAPHICS = new createjs.Graphics()
    .beginFill("#0887d7")
    .drawRect(0, 0, CANVAS_WIDTH, LAYER_HEIGHT);

class Drawer {
    constructor(tree) {
        this.tree = tree;
        this.stage = null;
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
            this._drawCall(childCalls[i], depth);
            this._drawRecursively(childCalls[i], depth + 1);
        }
    }

    static _flipY(y) {
        return CANVAS_HEIGHT - y - LAYER_HEIGHT;
    };

    _createSection() {
        $("main").append(templates.tree.getSectionForThread({threadId: this.tree.getThreadid()}).content);
    };

    _drawCall(call, depth) {
        const shape = new createjs.Shape(RECT_GRAPHICS);
        shape.setTransform(0, Drawer._flipY(depth * 16));
        this.stage.addChild(shape);
    }
}
