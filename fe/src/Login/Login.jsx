import axios from "axios";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

function Login (){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

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

    return (
        <div>
            <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)}></input><br></br>
            <input placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)}></input>
            <a >Forgot password?</a>
            <button onClick={handleLogin}>Login</button>
            <br></br>
            <p onClick={handleAdmin}>Admin</p>
            <p onClick={handleTeacher}>Teacher</p>
        </div>
    )
}

export default Login;