//void

function func1(): string {
    return 'hello'
}

function func2(): void {
    console.log('hello')
}

let a: void;
// a = null 안됨
// 오직 undefined만 가능
a = undefined;

// never
// 반환이 불가능한 타입 : 정상적으로 종료되지 않는 경우
function func3(): never {
    while (true) { }
}

function func4() :never {
    throw new Error();
}

let b : never;
// b에는 어떠한 값도 저장할 수 없다.
// b = null;
// b = undefined;
