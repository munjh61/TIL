type Dog = {
  name: string;
  isBark: boolean;
};

type Cat = {
  name: string;
  isScratch: boolean;
};

type Animal = Dog | Cat;

function isDog(animal: Animal) {
  return (animal as Dog).isBark !== undefined;
}

function isCat(animal: Animal): animal is Cat {
  return (animal as Cat).isScratch !== undefined;
}

function warning(animal: Animal) {
  // if('isBark' in animal){
  // 강아지
  //  else if('isScratch' in animal){
  // 고양이
  // }
  if (isDog(animal)) {
    // 강아지
  } else if (isCat(animal)) {
    // 고양이
  }
}
