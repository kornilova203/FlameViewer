class Call {
    constructor(name, desc, isStatic, parametersStr, startTime) {
        const dot = name.lastIndexOf('.');
        this.className = name.substring(0, dot);
        this.methodName = name.substring(dot + 1).replace("<", "&lt;").replace(">", "&gt;");
        this.startTime = startTime;
        this.duration = 0;
        this.desc = desc;
        this.isStatis = isStatic;
        this.parameters = Call.createParameters(parametersStr);
        this.returnVal = null;
        this.calls = [];
    }

    static createParameters(parametersStr) {
        if (parametersStr === "") { // if there is no parameters
            return [];
        }
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

    /**
     * Generate popup element
     * @returns {string}
     * @private
     */
    _generatePopup() {
        const parameters = this._getParametersForPopup();
        return '<div class="detail">' +
            '<h3>' + this.className + '.<b>' + this.methodName + '</b></h3>' +
            '<p>Start time: ' + Math.round(this.startTime / 10000) / 100 + ' ms</p>' +
            '<p>Duration: ' + Math.round(this.duration / 10000) / 100 + ' ms</p>' +
            parameters +
            '<p>Return value: ' + this.returnVal.toString() + '</p>' +
            '</div>';
    }

    _getParametersForPopup() {
        let string = "";
        for (let i in this.parameters) {
            //noinspection JSUnfilteredForInLoop
            string = string.concat('<p>' + this.parameters[i].toString() + '</p>');
        }
        return string;
    }
}