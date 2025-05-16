var mas = ["/images/holiday/dr2021/1.jpg","/images/holiday/dr2021/2.jpg","/images/holiday/dr2021/3.jpg",
"/images/holiday/dr2021/4.jpg","/images/holiday/dr2021/5.jpg","/images/holiday/dr2021/6.jpg"]
var to = -1;  // Счетчик, указывающий на текущую картинки


// устанавливаем триггер для модального окна (название можно изменить)
//const modalTrigger = document.getElementsByClassName("trigger")[0];

// получаем ширину отображенного содержимого и толщину ползунка прокрутки
const windowInnerWidth = document.documentElement.clientWidth;
const scrollbarWidth = parseInt(window.innerWidth) - parseInt(windowInnerWidth);

// привязываем необходимые элементы
const bodyElementHTML = document.getElementsByTagName("body")[0];
const modalBackground = document.getElementsByClassName("modalBackground")[0];
const modalClose = document.getElementsByClassName("modalClose")[0];
const modalActive = document.getElementsByClassName("modalActive")[0];

// функция для корректировки положения body при появлении ползунка прокрутки
function bodyMargin() {
    bodyElementHTML.style.marginRight = "-" + scrollbarWidth + "px";
}

// при длинной странице - корректируем сразу
bodyMargin();

// событие нажатия на триггер открытия модального окна
//modalTrigger.addEventListener("click", function () {
function modalWindow(srcI){
    // делаем модальное окно видимым
    modalBackground.style.display = "block";

    // если размер экрана больше 1366 пикселей (т.е. на мониторе может появиться ползунок)
    if (windowInnerWidth >= 1366) {
        bodyMargin();
    }

    // позиционируем наше окно по середине, где 250 - половина ширины модального окна
    modalActive.style.left = "calc(50% - " + (300 - scrollbarWidth / 2) + "px)";

 var modalImg = document.getElementById("modalImg");
 modalImg.src=srcI;

for (var i = 0 ; i < mas.length; i++)
 {
    if (mas[i] == modalImg.src)   // Как только встретилась
     {
      to = i;  // Задаем текущее значение счетчику
      }
}
}
//});

function right_arrow() // Открытие следующей картинки(движение вправо)
{
 var obj = document.getElementById("modalImg");
  if (to < mas.length-1)  to++
   else
     to = 0;
     obj.src = mas[to];
     //setCookie("foo", mas[to] , "", "/");	 // запоминаем текущую картинку
}

function left_arrow()
{
 var obj = document.getElementById("modalImg");
if (to > 0) to--;
  else
    to = mas.length-1;
    obj.src = mas[to];
    //setCookie("foo", mas[to] , "", "/");	 // запоминаем текущую картинку
}


// нажатие на крестик закрытия модального окна
modalClose.addEventListener("click", function () {
    modalBackground.style.display = "none";
    if (windowInnerWidth >= 1366) {
        bodyMargin();
    }
});

// закрытие модального окна на зону вне окна, т.е. на фон
modalBackground.addEventListener("click", function (event) {
    if (event.target === modalBackground) {
        modalBackground.style.display = "none";
        if (windowInnerWidth >= 1366) {
            bodyMargin();
        }
    }
});
