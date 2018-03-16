/**
 * Created by ace1_ on 16-Jun-17.
 */
import * as passport from "passport";
import * as express from "express";
import * as logger from "morgan";
import * as bodyParser from "body-parser";
import * as expressValidator from "express-validator";


import PostRouter from "./routes/PostRouter";
import HashRouter from "./routes/HashRouter";
import ProfileRouter from "./routes/ProfileRouter";
import {register, login} from "./routes/authentication";
require("./config/passport");
const flash = require("connect-flash");

//Set up mongoose connection
let mongoose = require('mongoose');
let mongoDB = 'mongodb://localhost/atd4';
let path = require('path');
mongoose.Promise = global.Promise;
mongoose.connect(mongoDB, {useMongoClient: true});
mongoose.connection.on('error', console.error.bind(console, 'MongoDB connection error:'));

const defaultGet = function (req, res) {
    res.sendFile('index.html', {root: './src/backend/frontend'});
};

// Creates and configures an ExpressJS web server.
class App {

    // ref to Express instance
    public express: express.Application;

    //Run configuration methods on the Express instance.
    constructor() {
        this.express = express();
        this.middleware();
        this.routes();
    }

    // Configure Express middleware.
    private middleware(): void {
        this.express.use(logger('dev'));
        this.express.use(require('express-session')({
            secret: 'session_secret',
            resave: false,
            saveUninitialized: false
        }));
        this.express.use(flash());
        this.express.use(express.static(path.join(__dirname, 'frontend')));
        this.express.use(express.static(path.join(__dirname, 'static')));
        this.express.use(passport.initialize());
        this.express.use(passport.session());
        this.express.use(bodyParser.json());
        this.express.use(bodyParser.urlencoded({extended: false}));
        this.express.use(expressValidator()); // Add this after the bodyParser middlewares!

    }

    // Configure API endpoints.
    private routes(): void {
        this.express.use('/api/v1/posts', PostRouter);
        this.express.use('/api/v1/hashtags', HashRouter);
        this.express.use('/api/v1/profile', ProfileRouter);
        this.express.post("/api/v1/register", register);
        this.express.post("/api/v1/logout", function (req, res) {
            req.logout();
            res.redirect('/');
        });
        this.express.post("/api/v1/login", login);
        this.express.post("/api/v1/bs", passport.authenticate("jwt", {
            successRedirect: '/loginSuccess',
            failureFlash: true
        }));

        //must be last?
        this.express.use(function clientErrorHandler(err, req, res, next) {
            if (req.xhr) {
                res.status(500).send({error: 'Something failed!'})
            } else {
                next(err)
            }
        });

        // All other paths redirect to index.html.
        this.express.use(function (req, res, next) {
            defaultGet(req,res);
        });
    }

}

export default new App().express;
