window.onload = function() {

  timerRandom = getRandomInt(3, 10);
  time = timerRandom*1000;
  random = getImage();
  setTimeout(cat, time);
  timeOff = timerOff();
  setTimeout(catNo, timeOff);

}

function timerOff() {
  if (random==0) {return time+6900;
  } else {return time+5500;}
}

function cat() {
  document.getElementById("textMessage").style.backgroundImage = "url('/images/"+random+"cat.gif')";
}

function catNo() {
  document.getElementById("textMessage").style.backgroundImage = "none";
}

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}

function getImage() {
 const random = Math.floor(Math.random() * 2);
 return random;
}