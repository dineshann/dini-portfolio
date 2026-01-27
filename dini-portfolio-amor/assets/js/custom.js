/* ----- Custom Scripts for Destiny template ----- */

jQuery(function ($) {
    "use strict";

    // get the value of the bottom of the #main element by adding the offset of that element plus its height, set it as a variable
    var mainbottom = $('#main').offset().top;

    // on scroll,
    $(window).on('scroll', function () {
        // we round here to reduce a little workload
        stop = Math.round($(window).scrollTop());
        if (stop > mainbottom) {
            $('.navbar').addClass('past-main');
            $('.navbar').addClass('effect-main')
        } else {
            $('.navbar').removeClass('past-main');
        }
    });

    // Collapse navbar on click
    $(document).on('click.nav', '.navbar-collapse.in', function (e) {
        if ($(e.target).is('a')) {
            $(this).removeClass('in').addClass('collapse');
        }
    });

    /* ------ jQuery for Easing min -- */
    // $(function () {
    //     $('a.page-scroll').bind('click', function (event) {
    //         var $anchor = $(this);
    //         $('html, body').stop().animate({
    //             scrollTop: $($anchor.attr('href')).offset().top
    //         }, 1500, 'easeInOutExpo');
    //         event.preventDefault();
    //     });
    // });

    /*----- Preloader ----- */
    $(window).load(function () {
        setTimeout(function () {
            $('#loading').fadeOut('slow', function () {});
        }, 3000);
    });

    /* --------- Wow Init -------*/
    new WOW().init();

    /*-----------------------------------
    ----------- Scroll To Top -----------
    ------------------------------------*/
    $(window).scroll(function () {
        if ($(this).scrollTop() > 1000) {
            $('#back-top').fadeIn();
        } else {
            $('#back-top').fadeOut();
        }
    });

    // scroll body to 0px on click
    $('#back-top').on('click', function () {
        $('#back-top').tooltip('hide');
        $('body,html').animate({
            scrollTop: 0
        }, 1500);
        return false;
    });

    /* ------ Animsition ----- */
    $(".animsition").animsition({
        inClass: 'fade-in',
        outClass: 'fade-out',
        inDuration: 1500,
        outDuration: 800,
        linkElement: '.animsition-link',
        // e.g. linkElement: 'a:not([target="_blank"]):not([href^="#"])'
        loading: true,
        loadingParentElement: 'body', //animsition wrapper element
        loadingClass: 'animsition-loading',
        loadingInner: '', // e.g '<img src="loading.svg" />'
        timeout: false,
        timeoutCountdown: 5000,
        onLoadEvent: true,
        browser: ['animation-duration', '-webkit-animation-duration'],
        // "browser" option allows you to disable the "animsition" in case the css property in the array is not supported by your browser.
        // The default setting is to disable the "animsition" in a browser that does not support "animation-duration".
        overlay: false,
        overlayClass: 'animsition-overlay-slide',
        overlayParentElement: 'body',
        transition: function (url) {
            window.location.href = url;
        }
    });
});