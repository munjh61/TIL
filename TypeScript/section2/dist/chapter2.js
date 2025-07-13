// 배열 선언 방식 1 : 대괄호
let numArr = [1, 2, 3];
let strArr = ['hello', 'Im', 'Moon'];
// 배열 선언 방식 2 : 제네릭 문법
let boolArr = [true, false, true];
// 배열의 요소가 여러 타입일 경우
let multiArr = [1, 'hello'];
// 다차원 배열의 타입을 정의하는 방법
let doubleArr = [
    [1, 2, 3],
    [4, 5]
];
// 튜플
// 길이와 타입이 고정된 배열
let tup1 = [1, 2];
// tup1 = [1, 2, 3] 길이가 맞지 않다
// tup1 = ['1', '2'] 타입이 맞지 않다
let tup2 = [1, '2', true];
tup1.push(1);
tup1.pop();
tup1.pop();
tup1.pop();
const users = [
    ['문땡땡', 1],
    ['송땡땡', 2],
    // [3,'김땡땡']
];
export {};
