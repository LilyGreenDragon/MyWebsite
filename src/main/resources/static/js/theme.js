var body;

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
themeFirst = document.getElementById("hiddenFieldTheme").value;
dat = document.getElementById("hiddenFieldDate").value;

window.onload = function() {
body.style.backgroundImage = 'url(' + themeFirst + ')';
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
var myDate = new Date(2025,0,27); //устанавливать месяц -1
//myDate.setFullYear(2023,2,9);
var today = new Date();
console.log(today);
console.log(myDate);

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

function getImage() {
  const random = Math.floor(Math.random() * 3);
  return "/images/"+random+".gif" ;

}
//Для темы окна
// Получаем элементы
        const modal = document.getElementById("myModal");
        const openModalBtn = document.getElementById("openModalBtn");
        const closeModalBtn = document.getElementById("closeModalBtn");
        const submitBtn = document.getElementById("submitBtn");
        var body = document.getElementsByTagName('body')[0];
        var theme;

        // Открыть модальное окно
        openModalBtn.onclick = function() {
            modal.style.display = "block";
        };

        // Закрыть модальное окно
        closeModalBtn.onclick = function() {
            modal.style.display = "none";
            body.style.backgroundImage = 'url(' + themeFirst + ')';
                            document.getElementById("hiddenFieldTheme").setAttribute("value", themeFirst);
        };


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

            //modal.style.display = "none"; // Закрыть окно после выбора



        // Закрытие окна при клике вне его
        window.onclick = function(event) {
            if (event.target == modal) {
                modal.style.display = "none";
                body.style.backgroundImage = 'url(' + themeFirst + ')';
                document.getElementById("hiddenFieldTheme").setAttribute("value", themeFirst);
            }
        };