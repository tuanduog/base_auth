import React, { useState } from "react";
import axios from "axios";

function PushAcc(){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fullname, setFullname] = useState("");
    const [role, setRole] = useState("");
    const acc = {
        email: email,
        password: password,
        fullname: fullname,
        role: role
    }
    const handlePush = async () => {
        const res = await axios.post("http://localhost:8080/auth/push-acc", acc, {withCredentials: true});
        console.log(res.data);
    }
    return(
        <div>
            <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)}></input><br></br>
            <input placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)}></input>
            <input placeholder="fullname"  value={fullname} onChange={(e) => setFullname(e.target.value)}></input>
            <input placeholder="role"  value={role} onChange={(e) => setRole(e.target.value)}></input>
            <button onClick={handlePush}>push</button>
        </div>
    )
}

export default PushAcc;