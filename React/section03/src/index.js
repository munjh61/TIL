console.log("hello");

// const moduleData = require("./math");
// console.log(moduleData.add(1, 2));
// console.log(moduleData.sub(1, 2));

import mul, { add, sub } from "./math.js";
// console.log(add(1, 2));
// console.log(sub(1, 2));
// console.log(mul(2, 3));

import randomColor from "randomcolor";

let color = randomColor();
console.log(color);
