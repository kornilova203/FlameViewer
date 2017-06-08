const DELIMITER = "⊗";
const PARAMETERS_DELIMITER = "⇑";

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

        drawThreads(threads);
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

        function drawThreads(threads) {
            /*
             this variables are for positioning of separate threads' threes
             */
            let startOfFirstThread = threads[Object.keys(threads)[0]];
            let finishOfLastThread = 0;
            for (let key in threads) {
                //noinspection JSUnfilteredForInLoop
                if (threads[key].startTime < startOfFirstThread) {
                    //noinspection JSUnfilteredForInLoop
                    startOfFirstThread = threads[key].startTime;
                }
                //noinspection JSUnfilteredForInLoop
                if (threads[key].startTime + threads[key].duration > finishOfLastThread) {
                    //noinspection JSUnfilteredForInLoop
                    finishOfLastThread = threads[key].startTime + threads[key].duration;
                }
            }
            console.log(startOfFirstThread);
            console.log(finishOfLastThread);
            for (let key in threads) {
                //noinspection JSUnfilteredForInLoop
                threads[key].draw($("main"), startOfFirstThread, finishOfLastThread - startOfFirstThread);
            }
        }
    }
});
