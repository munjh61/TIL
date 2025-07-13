// any
// 특정 변수의 타입을 고정하고 싶지 않을 때

let anyVar:any = 10;
// 초기값에 상관없이 값을 넣을 수 있다.
anyVar = 'hello'
anyVar = true;
anyVar = {};
anyVar = () => {};

// 어디에 넣어도 빨간 줄이 나오지 않는다.
let num: number = 10;
num = anyVar

// 하지만 컴파일 할 시에 오류가 나기 때문에 지양하도록 한다.


// unknown
let unKnownVar: unknown;
// any처럼 초기값에 상관없이 값을 넣을 수 있다.
unKnownVar = ''
unKnownVar = 1;

// 하지만 넣는 것은 안된다.
// num = unKnownVar

// 타입 정제를 하여야만 넣을 수 있다.
if(typeof unKnownVar === 'number'){
    num= unKnownVar
}