$(document).ready(function () {
//SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS

const pdfjsLib = window['pdfjsLib'] || window['pdfjs-dist/build/pdf'];
pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.16.105/pdf.worker.min.js';

let pdf;

$(document).on('click', '#upload-pdf2html', function(){

    pdf = $('#exam-pdf')[0].files[0];

    if(!pdf){
        alert('파일을 선택하세요.');
        return;
    }

    // 서버로 pdf 전송
    let formData = new FormData();
    formData.append("pdf", pdf);
    formData.append("pdfName", pdf.name);

    $.ajax({
        url: "/pdfToHtml",
        method: "POST",
        processData: false,
        contentType: false,
        data: formData,
        success: function(response) {

            if (response === 'err') {
                alert("서버 에러입니다.");
                return;
            }


            $('#pdf-upload-ar').css('display', 'none');

            $('#extract-data').css('display', 'flex');
            $('#extract-data').append(response);
        },
        error: function(xhr, status, error) {
            alert("서버 오류");
        }
    });


});


$(document).on('click', '#upload-pdf2canvas', function(){

    pdf = $('#exam-pdf')[0].files[0];

    if(!pdf){
        alert('파일을 선택하세요.');
        return;
    }

    const reader = new FileReader();
    reader.readAsArrayBuffer(pdf);

    reader.onload = function () {
        const arrayBuffer = reader.result;
        const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });

        loadingTask.promise
            .then(function (pdf) {
                let totalPages = pdf.numPages;

                function renderPage(pageNum) {
                    pdf.getPage(pageNum).then(function (page) {
                        const scale = 2.5; // 해상도
                        const viewport = page.getViewport({ scale });

                        // Canvas 생성
                        const canvas = $('<canvas></canvas>')[0];
                        const context = canvas.getContext('2d');


                        canvas.width = viewport.width;
                        canvas.height = viewport.height;


                        $(canvas).css({
                            width: viewport.width / scale + "px",
                            height: viewport.height / scale + "px"
                        });


                        page.render({ canvasContext: context, viewport }).promise.then(function () {
                            // Canvas를 이미지로 변환 (고해상도 유지)
                            const img = $('<img>')
                                .attr('src', canvas.toDataURL('image/png'))
                                .attr('data-page', pageNum)
                                .css({
                                    width: "57%", // 반응형 크기 조절
                                    "max-width": viewport.width + "px",
                                    "height": "auto" // 가로·세로 비율 유지
                                });

                            $('#pdf-upload-ar').css('display', 'none');
                            $('.work-form').css('display' , 'flex');

                            $('#extract-data').css('display', 'flex');

                            $('#extract-data').append(img);

                            // 다음 페이지 처리
                            if (pageNum < totalPages) {
                                renderPage(pageNum + 1);
                            }
                        });
                    });
                }

                // 첫 페이지부터 렌더링 시작
                renderPage(1);
            })
            .catch(function (error) {
                console.error('PDF 로드 중 오류 발생:', error);
            });
    };

});



//////////////////////////////////////////////////////////////////////////////


let isCapturing = false; // 캡처중 여부
let startX, startY, endX, endY;

const $captureArea = $("#capture-area-2");

let capAr = $('#extract-data');

$(document).on('click', '#select-q', function(){

    if(isCapturing){ // 캡처중이면
        capAr.css('cursor' , 'default');
        isCapturing = false;
    }else{ // 캡처중 아니면
        capAr.css('cursor' , 'crosshair');
        isCapturing = true;
    }

});


$(document).on("mousedown", function (e) {
    if (!isCapturing) return;

    startX = e.pageX;
    startY = e.pageY;

    $captureArea.css({
        left: startX + "px",
        top: startY + "px",
        width: "0px",
        height: "0px",
        display: "block",
    });
});


$(document).on("mousemove", function (e) {
    if (!isCapturing) return;

    endX = e.pageX;
    endY = e.pageY;

    const width = Math.abs(endX - startX);
    const height = Math.abs(endY - startY);

    $captureArea.css({
        width: width + "px",
        height: height + "px",
        left: Math.min(startX, endX) + "px",
        top: Math.min(startY, endY) + "px",
    });
});


$(document).on("mouseup", function (event) {

    if (!isCapturing) return;

    $captureArea.css('display' , 'none');

    capAr.css('cursor' , 'default');
    isCapturing = false;

    var formData = new FormData();
    formData.append("left", parseInt($captureArea.css('left')));
    formData.append("top", parseInt($captureArea.css('top')));
    formData.append("width", parseInt($captureArea.css('width')));
    formData.append("height", parseInt($captureArea.css('height')));
    formData.append("type", $('input[name="q-type"]:checked').val());
    formData.append("pdf", pdf);
    $.ajax({
        url: "/getText",
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            // 서버로부터 응답을 받았을 때 처리
        },
        error: function(xhr, status, error) {
            alert("서버 에러");
        }
    });




});















//EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
});