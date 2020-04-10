/** @format */

const express = require('express');
const dotenv = require('dotenv');

// Load config file
dotenv.config({ path: './config/config.env' });

const app = express();
// Routing
// Deliver Transcription
app.get('/api/transcription', (req, res) => {
	res.status(200).json({ success: true, msg: 'Transcription downloading...' });
});
app.post('/api/transcribe', (req, res) => {
	res.status(200).json({ success: true, msg: 'Transcription starting...' });
});
const PORT = process.env.PORT || 5000;
app.listen(
	PORT,
	console.log(`Server running in ${process.env.NODE_ENV} mode on port ${PORT}`),
);
