import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./Login/Login";
import Admin_Homepage from "./Admin_Homepage";
import Teacher_Homepage from "./Teacher_Homepage";
import PushAcc from "./PushAcc";
import ResetPassword from "./ResetPassword";

function App () {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login/>}></Route>
                <Route path="home-admin" element={<Admin_Homepage/>}></Route>
                <Route path="home-teacher" element={<Teacher_Homepage/>}></Route>
                <Route path="push-acc" element={<PushAcc/>}></Route>
                <Route path="reset-password" element={<ResetPassword/>}></Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App;