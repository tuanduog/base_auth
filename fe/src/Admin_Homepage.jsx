import React, { useState } from "react";
import './adhome.css';
import axios from "axios";

function Admin_Homepage() {
    const [file, setFile] = useState(null);

    const handleFileChange = (e) => {
        if (e.target.files) {
            setFile(e.target.files[0]);
        }
    };

    const handleUpload = async () => {
        if (file) {
            console.log('Uploading file...');

            const formData = new FormData();
            formData.append('file', file);

            try {
                const res = await axios.post('http://localhost:8080/admin/import-accounts', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                    withCredentials: true
                });
                console.log(res.data);
            } catch (error) {
                console.error(error);
            }
        }
    };

    return (
        <>
            <h2>Welcome to admin homepage</h2>
            <input type="file" onChange={handleFileChange} />
            {/* {file && <p>Selected file: {file.name}</p>} */}
            <button style={{ marginBottom: '10px' }} onClick={handleUpload}>Import data</button>

            <table>
                <thead>
                    <tr>
                        <th>id</th>
                        <th>email</th>
                        <th>password</th>
                        <th>fullname</th>
                        <th>role</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
        </>
    );
}

export default Admin_Homepage;
