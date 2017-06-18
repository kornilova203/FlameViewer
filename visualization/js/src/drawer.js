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

    drawTree () {
        this._createSection();
        this.stage = new createjs.Stage("canvas-" + this.tree.getThreadid());

        const shape = new createjs.Shape(RECT_GRAPHICS);
        shape.setTransform(0, Drawer._flipY(0));
        this.stage.addChild(shape);

        const shape2 = new createjs.Shape(RECT_GRAPHICS);
        shape2.setTransform(0, Drawer._flipY(16), 0.7);
        this.stage.addChild(shape2);

        this.stage.update();
    };

    static _flipY (y) {
        return CANVAS_HEIGHT - y - LAYER_HEIGHT;
    };

    _createSection () {
        $("main").append(templates.tree.getSectionForThread({threadId: this.tree.getThreadid()}).content);
    };
}
