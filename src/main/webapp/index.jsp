<html>
<body>
<h2>Hello World!</h2>

springmvc上传文件
<form action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="springmvc文件上传提交">
</form>
富文本上传文件
<form action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="富文本文件上传提交">
</form>
</body>
</html>
