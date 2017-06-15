// const $ = require('jquery');
// const createjs = require('createjs-easeljs');

$(window).on("load", () => {
    const g = new createjs.Graphics();
    const command = g.beginFill("red").command;
    g.drawRect(0, 0, 30, 40);
    // command.style = "blue";

    const stage = new createjs.Stage("demoCanvas");
    const shape = new createjs.Shape(g);
    stage.addChild(shape);
    stage.update();
    // shape.setTransform(undefined, undefined,20);
    createjs.Ticker.timingMode = createjs.Ticker.RAF_SYNCHED;
    createjs.Ticker.setFPS(30);
    createjs.Ticker.addEventListener("tick", (event) => {
        shape.setTransform(undefined, undefined, 2);
        stage.update();
    });

});