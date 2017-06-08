const LAYER_HEIGHT = 23;

class Thread {
    /**
     * Create new Thread
     */
    constructor(threadId, startTime) {
        this.threadId = threadId;
        this.calls = [];
        this.currentDepth = 0; // current currentDepth of tree
        this.startTime = startTime;
        this.duration = 0;
        this.maxDepth = 0;
    }

    startMethod(methodName, methodDesc, isStatic, parameters, startTime) {
        let calls = this.calls;
        for (let i = 0; i < this.currentDepth; i++) {
            calls = calls[calls.length - 1].calls;
        }
        calls.push(new Call(methodName, methodDesc, isStatic, parameters, startTime));
        this.currentDepth++;
        if (this.currentDepth > this.maxDepth) {
            this.maxDepth = this.currentDepth;
        }
    }

    finishMethod(methodName, methodDesc, isStatic, returnVal, finishTime) {
        if (this.currentDepth === 0) {
            throw new Error("There is more method exits than method starts");
        }
        let call = this.calls[this.calls.length - 1];
        for (let i = 1; i < this.currentDepth; i++) {
            call = call.calls[call.calls.length - 1];
        }
        if (call.name !== methodName.replace("<", "&lt;").replace(">", "&gt;")) {
            throw new Error("Finish method is not a method which was recently started");
        }
        call.finishCall(returnVal, finishTime);
        this.duration = finishTime - this.startTime; // update duration of thread
        this.currentDepth--;
    }

    /**
     * Create hierarchical <ol> tree in given html element
     * @param htmlElement element in which tree will be created
     * @param startOfFirstThread
     * @param commonDuration
     */
    draw(htmlElement, startOfFirstThread, commonDuration) {
        const section = this.createSection_(htmlElement);
        const div = this.createDiv_(section, startOfFirstThread, commonDuration);
        this.recursivelyCreateList_(div, this, 0, 0);
        $('ol').css("height", LAYER_HEIGHT * 2 + "px");
        $('li').css("height", LAYER_HEIGHT + "px");
        $('.method-name').css("line-height", LAYER_HEIGHT + "px");
    }

    /**
     * Create section in html element
     * @param htmlElement
     * @returns created section
     * @private
     */
    createSection_(htmlElement) {
        const section = $("<section></section>").appendTo(htmlElement);
        section.append($("<h2>" + this.threadId + "</h2>"));
        section.addClass("threadId" + this.threadId);
        return section;
    }

    /**
     * Create div for call-tree
     * @param section in which div will be created
     * @param startOfFirstThread
     * @param commonDuration
     * @returns created tree
     * @private
     */
    createDiv_(section, startOfFirstThread, commonDuration) {
        const div = $("<div></div>").appendTo(section);
        div.addClass("call-tree");
        div.css("height", this.maxDepth * LAYER_HEIGHT + 20);
        div.css("left", (this.startTime - startOfFirstThread) / commonDuration * 100 + "%");
        div.css("width", (this.duration) / commonDuration * 100 + "%");
        div.css("opacity", 0.95);
        return div;
    }

    /**
     * Build hierarchical list of nodes for one thread
     * @param htmlElement current <li> element which may be appended with <ol> (child nodes)
     * @param call
     * @private
     */
    recursivelyCreateList_(htmlElement, call) {
        if (call.calls.length === 0) {
            return;
        }
        const ol = $("<ol></ol>").appendTo(htmlElement);
        for (let i = 0; i < call.calls.length; i++) { // do not use for-in because order is important
            const childCall = call.calls[i];
            const newLi = $(`<li><p class="method-name">${childCall.name}</p></li>`).appendTo(ol);
            this.createPopup_(newLi, childCall);
            const left = (childCall.startTime - call.startTime) / call.duration;
            newLi.css("left", (left * 100) + "%");
            newLi.css("width", (childCall.duration) / call.duration * 100 + "%");
            this.recursivelyCreateList_(newLi, childCall);
        }
    }

    /**
     * Create popup which shows start time and duration
     * @param newLi html element for which popup will be showed
     * @param childNode node which contains information about time
     * @private
     */
    createPopup_(newLi, childNode) {
        const popup = $(Thread.generatePopup_(childNode.name, childNode.startTime,
            childNode.duration, childNode.parameters))
            .appendTo($(".threadId" + this.threadId));
        newLi.bind("mousemove", (e) => {
            newLi.css("background", "#5457FF"); // highlight current <li> element
            popup.show().css("top", e.pageY).css("left", e.pageX);
            return false;
        });
        newLi.bind("mouseout", () => {
            newLi.css("background", "");
            popup.hide();
            return false;
        });
    }

    /**
     * Generate popup element
     * @param methodName
     * @param className
     * @param startTime
     * @param duration
     * @returns {string}
     * @private
     * @param arg
     */
    static generatePopup_(methodName, startTime, duration, arg) {
        return '<div class="detail">' +
                    '<h3>' + methodName + '</h3>' +
                    '<p>Start time: ' + Math.round(startTime / 10000) / 100 + ' ms</p>' +
                    '<p>Duration: ' + Math.round(duration / 10000) / 100 + ' ms</p>' +
                    '<p>Argument: ' + arg + '</p>' +
            '</div>';
    }
}