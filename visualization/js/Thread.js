class Thread {
    /**
     * Create new Thread
     */
    constructor(threadId, startTime) {
        this.threadId = threadId;
        this.calls = [];
        this.depth = 0; // current depth of tree
        this.startTime = startTime;
        this.duration = 0;
        // this.layerHeight = 23; // layer height in pixels
    }

    startMethod(methodName, methodDesc, isStatic, parameters, startTime) {
        let calls = this.calls;
        for (let i = 0; i < this.depth; i++) {
            calls = calls[calls.length - 1].calls;
        }
        calls.push(new Call(methodName, methodDesc, isStatic, parameters, startTime));
        this.depth++;
    }

    finishMethod(methodName, methodDesc, isStatic, returnVal, finishTime) {
        if (this.depth === 0) {
            throw new Error("There is more method exits than method starts");
        }
        let call = this.calls[this.calls.length - 1];
        for (let i = 1; i < this.depth; i++) {
            call = call.calls[call.calls.length - 1];
        }
        if (call.name !== methodName) {
            throw new Error("Finish method is not a method which was recently started");
        }
        call.finishCall(returnVal, finishTime);
        this.duration = finishTime - this.startTime; // update duration of thread
        this.depth--;
    }

    /**
     * Create hierarchical <ol> tree in given html element
     * @param htmlElement element in which tree will be created
     */
    createList(htmlElement) {
        const section = this.createSection_(htmlElement);
        const div = this.createDiv_(section);
        this.recursivelyCreateList_(div, this.tree, 0, 0);
        $('ol').css("height", this.layerHeight * 2 + "px");
        $('li').css("height", this.layerHeight + "px");
        $('.method-name').css("line-height", this.layerHeight + "px");
    }

    /**
     * Create section in html element
     * @param htmlElement
     * @returns created section
     * @private
     */
    createSection_(htmlElement) {
        const section = $("<section></section>").appendTo(htmlElement);
        section.append($("<h2>" + this.name + "</h2>"));
        section.addClass(this.name);
        return section;
    }

    /**
     * Create div for call-tree
     * @param section in which div will be created
     * @returns created tree
     * @private
     */
    createDiv_(section) {
        const div = $("<div></div>").appendTo(section);
        div.addClass("call-tree");
        div.css("height", this.depth * this.layerHeight + 20);
        div.css("left", (this.tree.startThreadTime - this.startOfFirstTread) / this.commonDuration * 100 + "%");
        div.css("width", (this.tree.duration) / this.commonDuration * 100 + "%");
        div.css("opacity", 0.95);
        return div;
    }

    /**
     * Build hierarchical list of nodes for one thread
     * @param htmlElement current <li> element which may be appended with <ol> (child nodes)
     * @param subTree
     * @private
     */
    recursivelyCreateList_(htmlElement, subTree) {
        if (subTree.nodes.length === 0) {
            return;
        }
        const ol = $("<ol></ol>").appendTo(htmlElement);
        for (let i = 0; i < subTree.nodes.length; i++) { // do not use for-in because order is important
            const childNode = subTree.nodes[i];
            const newLi = $("<li><p class='method-name'>" + childNode.methodName + "</p></li>").appendTo(ol);
            this.createPopup_(newLi, childNode);
            const left = (childNode.startTime - subTree.startTime) / subTree.duration;
            newLi.css("left", (left * 100) + "%");
            newLi.css("width", (childNode.duration) / subTree.duration * 100 + "%");
            this.recursivelyCreateList_(newLi, childNode);
        }
    }

    /**
     * Create popup which shows start time and duration
     * @param newLi html element for which popup will be showed
     * @param childNode node which contains information about time
     * @private
     */
    createPopup_(newLi, childNode) {
        const popup = $(CallTree.generatePopup_(childNode.methodName, childNode.className,
            childNode.startTime, childNode.duration, childNode.arg))
            .appendTo($("." + this.name));
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
    static generatePopup_(methodName, className, startTime, duration, arg) {
        return '<div class="detail"><h3>' + className + ".<b>" + methodName + '</b></h3>' +
            '<p>Start time: ' + Math.round(startTime / 10000) / 100 + ' ms</p>' +
            '<p>Duration: ' + Math.round(duration / 10000) / 100 + ' ms</p>' +
            '<p>Argument: ' + arg + '</p></div>';
    }
}