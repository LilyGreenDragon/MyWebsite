var body;
var theme;
var dat;
var timerRandom;
var time;

var curPosX = 0;
var curPosY = 0;
var interval;
var n = 5; // На сколько двигать за раз
var width = document.documentElement.clientWidth; // Ширина экрана
var height = document.documentElement.clientHeight; // Высота экрана
var widthB = document.body.clientWidth;
var imgWidth = 80; // Ширина картинки
var imgHeight = 40; // Высота картинки
var img1 = document.getElementById("img1");

//curPosX = width*0.12; //если надо не с начала экрана

body = document.getElementsByTagName('body')[0];
theme = document.getElementById("hiddenFieldTheme").value;
dat = document.getElementById("hiddenFieldDate").value;

window.onload = function() {
body.style.backgroundImage = 'url(' + theme + ')';
datNY();
datHB();
img1.src = getImage();
img1.style.visibility = 'hidden'
timerRandom = getRandomInt(5, 180);
time = timerRandom*1000;
setTimeout(interval, time);

}

function datNY() {
//alert(dat);
var myDate = new Date(2023,2,10);
//myDate.setFullYear(2023,2,9);
var today = new Date();
//console.log(today);

if (myDate.getMonth()==today.getMonth() && myDate.getDate()==today.getDate()) {
 document.getElementById("middletext").style.backgroundImage = "url('/images/NG1.jpg')";
 document.getElementById("i").innerHTML = "";
 document.getElementById("about").innerHTML = "";
}
}

function datHB() {
var myDate = new Date(dat);
//myDate.setFullYear(2023,2,9);
var today = new Date();
if (myDate.getMonth()==today.getMonth() && myDate.getDate()==today.getDate()) {
 document.getElementById("middletext").style.backgroundImage = "url('/images/DR3.jpg')";
 document.getElementById("i").innerHTML = "";
 document.getElementById("about").innerHTML = "";
}
}

function interval() {
interval = setInterval(move, 100);
}

function move() {
  img1.style.visibility = 'visible'
  img1.style.right = (curPosX += n) + "px";
 // img1.style.top = (curPosY += n) + "px";
 console.log(widthB);
  if (curPosX == 1600) {
    clearInterval(interval);
    img1.style.visibility = 'hidden';
  }
}

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}

function original() {
body.style.backgroundImage = "none";
document.getElementById("hiddenFieldTheme").setAttribute("value", "");
}

function plane() {
theme ='/images/plane.jpg'
body.style.backgroundImage = 'url(' + theme + ')';
document.getElementById("hiddenFieldTheme").setAttribute("value", theme);
}

function car() {
theme ='/images/car.jpg'
body.style.backgroundImage = 'url(' + theme + ')';
document.getElementById("hiddenFieldTheme").setAttribute("value", theme);
}

function city() {
theme ='/images/city.jpg'
body.style.backgroundImage = 'url(' + theme + ')';
document.getElementById("hiddenFieldTheme").setAttribute("value", theme);
}

function flower() {
theme ='/images/flower.jpg'
body.style.backgroundImage = 'url(' + theme + ')';
document.getElementById("hiddenFieldTheme").setAttribute("value", theme);
}

function getImage() {
  const random = Math.floor(Math.random() * 3);
  return "/images/"+random+".gif" ;
}

