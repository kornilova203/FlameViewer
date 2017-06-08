const DELIMITER = "⊗";
const PARAMETERS_DELIMITER = "⇑";

class Parameter {
    constructor(type, val) {
        this.type = type;
        this.val = val;
    }
}

class Call {
    constructor(name, desc, isStatic, parametersStr, startTime) {
        this.name = name;
        this.startTime = startTime;
        this.duration = 0;
        this.desc = desc;
        this.isStatis = isStatic;
        this.parameters = Call.createParameters(parametersStr);
        this.returnVal = null;
        this.calls = [];
    }

    static createParameters(parametersStr) {
        const parameters = [];
        const values = parametersStr.split(PARAMETERS_DELIMITER);
        for (let i in values) {
            //noinspection JSUnfilteredForInLoop
            parameters.push(new Parameter("noType", values[i]));
        }
        return parameters;
    }

    finishCall(returnVal, finishTime) {
        this.returnVal = new Parameter("noType", returnVal);
        this.duration = finishTime - this.startTime;
    }
}

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

/**
 * Remove elements in dome
 * it is needed when upload new file without refreshing page
 */
function clearDom() {
    $("main section").remove();
}

/**
 * Main function
 */
$(window).on("load", function () {
    const input = document.querySelectorAll('.inputfile')[0];
    const label = input.nextElementSibling;
    const labelVal = label.innerHTML;

    $('#file').on('change', function (e) {
        const file = e.target.files[0]; // FileList object
        const reader = new FileReader();

        // Closure to capture the file information.
        reader.onload = (function (theFile) {
            reader.readAsText(theFile);
            reader.addEventListener("load", function () {
                try {
                    const fileData = reader.result;
                    clearDom();
                    processData(fileData);
                    $('main').css("margin-top", 30);
                    changeName(e);
                }
                catch (exception) {
                    console.error(exception);
                    // alert("Invalid file :(");
                }
            });
        })(file);

        /**
         * Change text on button
         * @param e
         */
        function changeName(e) {
            let fileName = '';
            if (this.files && this.files.length > 1)
                fileName = ( this.getAttribute('data-multiple-caption') || '' ).replace('{count}', this.files.length);
            else
                fileName = e.target.value.split('\\').pop();

            if (fileName)
                label.querySelector('span').innerHTML = fileName;
            else
                label.innerHTML = labelVal;
        }
    });

    /**
     * Create call-trees
     * @param fileData
     */
    function processData(fileData) {
        const lines = fileData.split("\n");
        const threads = {};

        for (let i = 0; i < lines.length - 1; i++) {
            const line = lines[i];
            const words = line.split(DELIMITER);
            const threadIdStr = words[0];
            const name = words[2];
            const desc = words[3];
            const isStatic = words[4] === "static";
            const parameters = words[5];
            const time = parseInt(words[6]);
            let thread = getOrCreateThread(threadIdStr, time);

            if (words[1] === "s") {
                thread.startMethod(name, desc, isStatic, parameters, time);
            } else if (words[1] === "f") {
                thread.finishMethod(name, desc, isStatic, parameters, time);
            } else {
                throw new Error("Invalid file");
            }

        }
        console.log(threads);
        // const startTimes = [];
        // const finishTimes = [];
        // for (let i in threads) {
        //     startTimes.push(threads[i].startThreadTime);
        //     finishTimes.push(threads[i].startThreadTime + threads[i].duration);
        // }
        // const startOfFirstThread = getMinOfArray(startTimes);
        // const finishOfFirstThread = getMaxOfArray(finishTimes);
        // for (let i in threads) {
        //     new CallTree(threads[i], startOfFirstThread, finishOfFirstThread).createList($('main'));
        // }
        // function getMaxOfArray(numArray) {
        //     return Math.max.apply(null, numArray);
        // }
        //
        // function getMinOfArray(numArray) {
        //     return Math.min.apply(null, numArray);
        // }

        function getOrCreateThread(threadIdStr, time) {
            const thread = threads[threadIdStr];
            if (thread === undefined) {
                return threads[threadIdStr] = new Thread(parseInt(threadIdStr), time);
            }
            return thread;
        }
    }
});
