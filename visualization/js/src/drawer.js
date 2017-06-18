const CANVAS_WIDTH = 700;
const CANVAS_HEIGHT = 400;
const LAYER_HEIGHT = 15;
const RECT_GRAPHICS = new createjs.Graphics()
    .beginFill("#0887d7")
    .drawRect(0, 0, CANVAS_WIDTH, LAYER_HEIGHT);

const drawer = {};

drawer._flipY = function (y) {
    return CANVAS_HEIGHT - y - LAYER_HEIGHT;
};

drawer._createSectionForThread = function (threadId) {
    $("main").append(templates.tree.getSectionForThread({threadId: threadId}).content);
};

drawer.drawTree = function (tree) {
    drawer._createSectionForThread(tree.getThreadid());
    const stage = new createjs.Stage("canvas-" + tree.getThreadid());

    const shape = new createjs.Shape(RECT_GRAPHICS);
    shape.setTransform(0, drawer._flipY(0));
    stage.addChild(shape);

    const shape2 = new createjs.Shape(RECT_GRAPHICS);
    shape2.setTransform(0, drawer._flipY(16), 0.7);
    stage.addChild(shape2);

    stage.update();
};
