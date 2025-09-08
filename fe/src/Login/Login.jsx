import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Login (){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const [showInput, setShowInput] = useState(false);
    const [emailConfirm, setEmailConfirm] = useState("");

    const handleLogin = async () => {
        try {
            const authRequest = { email: email, password: password };
            const res = await axios.post("http://localhost:8080/auth/login", authRequest, {
                withCredentials: true,
                headers: {
                "Content-Type": "application/json"
                }
            });
            if (res.status === 200) {
                console.log(res.data);
                alert("Login success");
            } else {
                console.log("Login failed", res.data);
                alert("Login failed: " + (res.data.error || "Unknown error"));
            }
        } catch (error) {
            console.log("Login failed", error.response?.data || error.message);
            alert("Login failed: " + (error.response?.data?.error || "Unknown error"));
        }
    };

    const forgotPassword = async () => {
        setShowInput(true);
    }

    const sendReset = async () => {
        try {
            const res = await axios.post("http://localhost:8080/auth/forgot-password", { email: emailConfirm });    
            console.log(res.data);
            alert("Reset password email sent");
        } catch (error) {
            console.log("Send reset email failed", error.response?.data || error.message);
            alert("Send reset email failed: " + (error.response?.data?.error || "Unknown error"));
        }
    }

    const handleAdmin = async () => {
        try {
            const res = await axios.get("http://localhost:8080/admin/hello", { withCredentials: true});
            console.log(res.data);
            navigate("/home-admin");
        } catch (error) {
            console.log("get admin failed", error);
        }
    }

    const handleTeacher = async () => {
        try {
            const res = await axios.get("http://localhost:8080/teacher/hello", { withCredentials: true});
            console.log(res.data);
            navigate("/home-teacher")
        } catch (error) {
            console.log("get teacher failed", error);
        }
    }

    useEffect(() => {
        const checkTokenExpired = async () => {
            const userParams = new URLSearchParams(window.location.search);
            const token = userParams.get('token');
            const res = await axios.get(`http://localhost:8080/auth/check-token/${token}`, {withCredentials: true});
            if(res.data.valid){
                navigate("/reset-password?token=" + token);
            } else {
                alert("Token is invalid or expired");
            }
        }
        checkTokenExpired();
    },[]);

    return (
        <div>
            <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)}></input><br></br>
            <input placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)}></input>
            <a onClick={forgotPassword}>Forgot password?</a>
            {showInput && (
                <div>
                    <input placeholder="email confirm" value={emailConfirm} onChange={(e) => setEmailConfirm(e.target.value)}></input>
                    <button onClick={sendReset}>Confirm</button>
                </div>
            )}
            <button onClick={handleLogin}>Login</button>
            <br></br>
            <p onClick={handleAdmin}>Admin</p>
            <p onClick={handleTeacher}>Teacher</p>
        </div>
    )
}

export default Login;