// 서로소 유니온 타입
// 교집합이 없는 타입들로만 만든 유니온 타입

type Admin = {
    tag: 'ADMIN'
    name: string
    kickCount: number
}
type Member = {
    tag: 'MEMBER'
    name: string
    point: number
}
type Guest = {
    tag: 'GUEST'
    name: string
    visitCount: number
}

type User = Admin | Member | Guest

// ADMIN -> {name}님 현재까지 {kickCount}명 강퇴했습니다.
// Member -> {name}님 현재까지 {point}포인트 모았습니다.
// Guest -> {name}님 현재까지 {visitCount}번 오셨습니다.
function login(user: User) {
    // 이런 식으로 나눌 순 있지만, 해당 속성이 누구인지 직관적이지 않다.
    if ('kickCount' in user) {
        console.log(`${user.name}님 현재까지 ${user.kickCount}명 강퇴했습니다.`)
    } else if ('point' in user) {
        console.log(`${user.name}님 현재까지 ${user.point}포인트 모았습니다.`)
    } else if ('visitCount' in user) {
        console.log(`${user.name}님 현재까지 ${user.visitCount}번 오셨습니다.`)
    }
}

function login2(user: User) {
    switch (user.tag) {
        case 'ADMIN':
            console.log(`${user.name}님 현재까지 ${user.kickCount}명 강퇴했습니다.`)
            break
        case 'MEMBER':
            console.log(`${user.name}님 현재까지 ${user.point}포인트 모았습니다.`)
            break
        case 'GUEST':
            console.log(`${user.name}님 현재까지 ${user.visitCount}번 오셨습니다.`)
            break
    }
}

// 비동기 작업의 결과를 처리하는 객체

type AsyncTask = {
    state: 'LOADING' | 'FAILED' | 'SUCCESS'
    error?: {
        message: string
    }
    response?: {
        data: string
    }
}

// 물음표가 있기 때문에 안전성이 떨어짐
function processResult(task: AsyncTask) {
    switch (task.state) {
        case 'LOADING':
            console.log('로딩 중')
            break
        case 'FAILED':
            console.log(`에러 발생 ${task.error?.message}`)
            break
        case 'SUCCESS':
            console.log(`성공 : ${task.response?.data}`)
            break
    }
}

type LoadingTask = {
    state: 'LOADING',
}

type FaildTask = {
    state: 'FAILED',
    error: {
        message: '오류 발생'
    }
}

type SuccessTask = {
    state: 'SUCCESS',
    response: {
        data: '데이터'
    }
}

type AsyncTask2 = LoadingTask| FaildTask | SuccessTask
function processResult2(task: AsyncTask2) {
    switch (task.state) {
        case 'LOADING':
            console.log('로딩 중')
            break
        case 'FAILED':
            console.log(`에러 발생 ${task.error.message}`)
            break
        case 'SUCCESS':
            console.log(`성공 : ${task.response.data}`)
            break
    }
}