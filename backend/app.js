const express = require('express');
const logger = require('morgan');
const indexRouter = require('./routes/index');
const app = express();

const allowLocalhostOnly = (req, res, next) => {
    if (req.hostname === 'localhost' || req.hostname === '127.0.0.1') {
        next();
    } else {
        res.status(403).send('Forbidden');
    }
};

app.use(allowLocalhostOnly);

app.listen(3000);

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

app.use("/", indexRouter);
