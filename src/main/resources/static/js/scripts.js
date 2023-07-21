let contentElement;
let url = 'content';
let data;
let fileName;
let progress = $('#report-progress');
let ajax;

function showContent(element)
{
    showProgress();
    let departments = document.getElementsByClassName('department');
    for (let i = 0; i < departments.length; i++)
    {
        $(departments[i]).removeClass('active-dep');
    }

    $(element).addClass('active-dep')

    contentElement = $('#content');
    contentElement.removeClass('icon-arrow-left').html('');
    fileName = element.getAttribute('value');
    data = 'department=' + fileName;

    ajaxRequest();
}

let ajaxRequest = function () {
    if (ajax)
        ajax.abort();
     ajax = $.ajax({
        type: 'GET',
        contentType: 'application/json',
        url: url,
        data: data,
        responseJSON: null
    }).done(function (data) {
        displaySuccess(data);
    }).fail(function (jqXHR) {
        displayError(jqXHR.responseJSON.error);
    });
    data = '';
}


function displaySuccess(data) {
    hideProgress();
    contentElement.removeClass('alert alert-danger').html(data);
}

function displayError(error) {
    hideProgress();
    contentElement.addClass('alert alert-danger').html(error);
}

function showSelection(element)
{
    let id = element.getAttribute('value');
    let contentList = document.getElementsByClassName('content');

    for (let i = 0; i < contentList.length; i++)
    {
        let content = contentList[i];
        $(content).addClass('d-none')
    }
    $(document.getElementById(id)).removeClass('d-none')

    let tabLinks = document.getElementsByClassName('tab-button');

    for (let i = 0; i < tabLinks.length; i++)
    {
        $(tabLinks[i]).removeClass('active');
    }
    $(element).addClass('active');
}

$(function () {
    $('#input-form').submit(function (){
        return false;
    });
});

function showContentWithPassword()
{
    let passwordElement = $('#password');
    showProgress();
    let password = passwordElement.val();
    if (!password)
    {
        hideProgress();
        passwordElement.addClass('alert alert-danger is-invalid').focus();
        return;
    }
    data = 'department=' + fileName + '&password=' + window.btoa(password);

    ajaxRequest();
}

function showProgress()
{
    progress.attr('hidden', false);
}

function hideProgress()
{
    progress.attr('hidden', true);
}

function showHiddenRows(row)
{
    let elements = document.getElementsByClassName(row.getAttribute('tag'));
    console.log(elements)

    for (let i = 0; i < elements.length; i++)
    {
            $(elements[i]).toggleClass('d-none');
    }
}