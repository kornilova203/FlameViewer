class Parameter {
    constructor(type, val) {
        this.type = type;
        this.val = val;
    }

    toString() {
        return this.type + " " + this.val;
    }
}