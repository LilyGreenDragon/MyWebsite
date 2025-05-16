
$( document ).ready(function() {
   var flag = $('#flag').text();
   console.log(flag);

   if (flag == "1") {$('form').animate({height: "toggle", opacity: "toggle"}, "slow");}
});


$('.message a').click(function(){
   $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
});
