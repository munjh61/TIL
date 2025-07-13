// 타입 좁히기

type Person = {
    name: string,
    age: number,
}

function func(value: number | string | Date | null | Person) {
    // value.toFixed() // 불가능
    // value.toUpperCase() // 불가능
    // value.getTime()

    // 타입 좁히기 (타입 가드)
    if (typeof value === 'number') {
        console.log(value.toFixed()) // 가능
    } else if (typeof value === 'string') {
        console.log(value.toUpperCase()) // 가능
// } else if (typeof value === 'object'){ // object는 좋지 않다. 너무 다양한 것이 올 수 있기 때문
    } else if (value instanceof Date) {
        console.log(value.getTime())
// } else if(value instanceof Person){ // type은 class가 아니기 때문에 instanceof를 사용할 수 없다.
    } else if(value && 'age' in value){ // value가 null일 가능성을 없애고, 속성에 특정 값이 있는지 확인하는 방식으로 해야한다.
        console.log(`${value.name}은 ${value.age}살입니다.`)
    }
}