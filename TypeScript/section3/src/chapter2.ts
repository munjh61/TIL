// unknown 타입
// 계층 최상위
function unknownExam() {
    // 업 캐스팅 모든 타입 가능
    let a: unknown = 1
    let b: unknown = 'hello'
    let c: unknown = true
    let d: unknown = null
    let e: unknown = undefined


    // 다운 캐스팅 불가능
    let unknownVar: unknown
    // let num:number = unknownVar
}

// never 타입
// 계층 최하위
function never() {
    function neverFunc(): never {
        while (true) { }
    }
    
    // 업 캐스팅 모든 타입 불가능
    let num:number = neverFunc()

    // 모든 다운 캐스팅 가능
    // let a: never = 1
    // let b: never = 'hello'
    // let c: never = true
    // let d: never = null
    // let e: never = undefined
}


// void
function voidExam(){
    function voidFunc():void{
        console.log('hi')
        // undefined의 슈퍼 타입이기 때문에
        // return undefined 가능하다.
    }
}

// any 타입
// 계층도를 무시함, 모든 타입의 슈퍼타입이면서 서브 타입
function anyExam(){
    let unknownVar: unknown
    let anyVar:any
    let undefinedVar : undefined
    let neverVar:never

    anyVar = unknownVar
    unknownVar = anyVar
    undefinedVar = anyVar
    // never 타입은 any타입이여도 할당 할 수 없다
    // neverVar = anyVar
}