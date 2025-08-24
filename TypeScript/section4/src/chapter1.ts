type Opp = (a: number, b: number) => number;

const add: Opp = (a, b) => a + b;
const sub: Opp = (a, b) => a - b;
const multiply: Opp = (a, b) => a * b;
const divide: Opp = (a, b) => a / b;

type Opp2 = {
    (a:number, b:number): number;
    name: string
}

const add2: Opp2 = (a, b) => a + b;
const sub2: Opp2 = (a, b) => a - b;
const multiply2: Opp2 = (a, b) => a * b;
const divide2: Opp2 = (a, b) => a / b;
add2.name