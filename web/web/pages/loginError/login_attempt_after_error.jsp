
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SdmMarket</title>

    <!-- Link the Bootstrap (from twitter) CSS framework in order to use its classes-->
    <link rel="stylesheet" href="login.css">

    <!-- Link jQuery JavaScript library in order to use the $ (jQuery) method-->
    <!-- <script src="common/jquery-2.0.3.min.js"></script>-->
    <!-- and\or any other scripts you might need to operate the JSP file behind the scene once it arrives to the client-->
</head>
<body>
<div class="container">
    <form method="GET" action="login">
        <div>
            <img src="../../resources/sdm_logo.png" alt="Super Duper Market" class="logo">
        </div>
        <hr>
        <div id ="user">
            <label for="username">User name: </label>
            <input type="text" id="username" name="username" class="" placeholder="Enter user name" required/>
        </div>
        <input type="radio" id="customer" name="typeofuser" value="customer" checked=true>
        <label for="customer">Customer </label>
        <input type="radio" id="owner" name="typeofuser" value="owner">
        <label for="owner">Owner</label><br><br>
        <input type="submit" value="Login" class = "login-button"/>
        <label style="color:red">Username already exists. Please enter a different username.</label>
    </form>

</div>
</body>
</html>