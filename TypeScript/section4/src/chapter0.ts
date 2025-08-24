function func(a: number, b: number) {
  return a + b;
}

const add = (a: number, b: number) => a + b;

function introduce (name = '문준호', tall?:number){
    console.log(`name : ${name}`)
    if(typeof tall === 'number')
    console.log(`tall : ${tall+10}`)
}
introduce('문준호')
introduce('문준호',173)

function getSum(...rest:number[]){
  let sum = 0;
  rest.forEach((it)=> sum += it)
  return sum
}