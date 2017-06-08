class Call {
    constructor(name, desc, isStatic, parametersStr, startTime) {
        this.name = name.replace("<", "&lt;").replace(">", "&gt;");
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