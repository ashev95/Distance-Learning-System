<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
    <link rel="shortcut icon" href="resources/dojo/images/favicon.ico" type="image/x-icon">
    <title>Система дистанционного обучения</title>

    <style>
        .loginBlock {
            position: fixed;
            top: 50%;
            left: 50%;
            margin-top: -214px;
            margin-left: -250px;
        }

        .field {
            height: 21px;
            /*font-size: 14px;*/
            border: 1px solid gray;
            border-radius: 5px;
            width: 100%;
            margin-bottom: 10px;
            margin-top: 3px;
            margin-left: 15px;
            background-color: white;
        }

        .color{
            color: rgb(63, 163, 255);
        }

        .enter{
            font-size:13pt;
            font-weight: bold;
            text-align: center;
        }

        .caption{
            font-size:11pt;
            font-weight: bold;
            padding: 1px;
            padding-left:15px;
        }

        .error {
            color: rgb(194, 0, 0);
            font-size: 11pt;
            text-align: center;
            padding: 5px 0px;
        }

        body {
            font-family: Verdana, serif;
        }

        table {
            font-size: 100%;
        }

        .button-link {
            padding: 6px 15px;
            background: rgb(232,232,232);
            color: rgb(75,75,75);
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            border: solid 1px rgb(168,168,168);
            text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.3);
            -webkit-box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4), 0 1px 1px rgba(0, 0, 0, 0.2);
            -moz-box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4), 0 1px 1px rgba(0, 0, 0, 0.2);
            box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4), 0 1px 1px rgba(0, 0, 0, 0.2);
            -webkit-transition-duration: 0.2s;
            -moz-transition-duration: 0.2s;
            transition-duration: 0.2s;
            -webkit-user-select:none;
            -moz-user-select:none;
            -ms-user-select:none;
            user-select:none;
            margin-right: -15px;
        }
        .button-link:hover {
            background: rgb(242,242,242);
            text-decoration: none;
        }

        .button-link:active {
            background: rgb(252,252,252);
        }
    </style>
</head>
<body text="#000000" bgcolor="#FFFFFF">
<form method="post" action="${contextPath}/login">
    <div class="loginBlock">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #e8efff; border-radius: 15px;">
            <tbody><tr valign="top"><td width="" style=""><div style="height:369px; width:500px;">
                <div align="center">
                    <table border="0" cellspacing="0" cellpadding="0">
                        <tbody><tr valign="top"><td width="379"></td><td style="font-size: 80pt">&nbsp;</td></tr>

                        <tr valign="top"><td class="color enter" width="379">Вход</td></tr>

                        <tr valign="top"><td class="error" style="min-height:10px" width="379">Истекла сессия</td></tr>

                        <tr valign="top"><td class="color caption" width="379">Имя пользователя</td></tr>

                        <tr valign="top"><td width="379">
                            <input name="username" value="" onkeypress="sendForm(event)" maxlength="50" autocomplete="on" class="field" autofocus="true"></td></tr>

                        <tr valign="top"><td class="color caption" width="379">Пароль</td></tr>

                        <tr valign="top"><td width="379">
                            <input name="password" value="" type="password" onkeypress="sendForm(event)" maxlength="255" class="field"></td></tr>
                        <tr valign="top"><td style="height:10px" width="379"><img width="1" height="1" border="0" alt=""></td></tr>

                        <tr valign="top"><td width="379">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                <tbody>
                                <tr valign="top">
                                    <td width="44%">&nbsp;&nbsp;</td>
                                    <td width="56%">
                                        <div align="right">
                                            <input type="button" onclick="submit()" value="Войти" class="button-link">
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td></tr>

                        <tr valign="top"><td width="379"></td><td style="font-size: 26pt">&nbsp;</td></tr>

                        <tr valign="top"><td width="379"></td></tr>
                        </tbody></table>
                </div></div></td></tr>
            </tbody></table>
    </div>

    <script>
        function sendForm(event){if(event.keyCode == 13){submit()};}
        function submit(){document.forms[0].submit()};
    </script>

</form>
<c:remove var = "SPRING_SECURITY_LAST_EXCEPTION" scope = "session" />
</body>
</html>