/**
 * Created by acepace on 22/06/2017.
 */
import {NextFunction, Request, Response} from "express";
import {User} from "../models/users";
import {generateToken} from "../config/passport";


export const isRightUser = function (req: Request, res: Response, next: NextFunction) {
    let querying_user = (req.params.display_name);
    let real_user = req.user.display_name;
    if (querying_user === real_user) {
        next()
    } else {
        res.status(403).send({message: "User in query does not matched logged in user"});
    }
};

export const login = function (req, res) {
    let payload = req.body;
    User.findOne({"display_name": payload.display_name}, "password_hash display_name", function (err, user) {
        let errorMsg = {error: 'Your login details could not be verified. Please try again.'};
        if (err) {
            req.flash(err);
            res.status(503);
            res.send();
        }


        if (!user) { //success
            req.flash(errorMsg);
            res.status(401);
            res.json(errorMsg);
            return;
        }

        user.comparePassword(user, payload.password, function (err, isMatch) {
            if (err) {
                res.flash(err);
                res.status(401);
                res.json(err);
                return;
            }
            if (!isMatch) {
                req.flash(errorMsg);
                res.status(401);
                res.json(errorMsg);
                return;
            }

            let token = generateToken(user);
            res.json({message: "ok", token: token});

        });
    });
};

export const register = function (req, res) {
    let validationSchema = {
        'email': {
            notEmpty: true,
            isEmail: {
                errorMessage: 'Invalid Email'
            }
        },
        'password': {
            notEmpty: true,
            errorMessage: 'Invalid Password' // Error message for the parameter
        },
        'display_name': { //
            notEmpty: true,
            isAlphanumeric: true,
            errorMessage: 'Invalid display name'
        }
    };
    req.check(validationSchema);


    req.getValidationResult().then(function (result) {
        if (!result.isEmpty()) {
            let errorMsg = JSON.stringify(result.mapped());
            res.status(400).send(errorMsg);
            return;
        }
        req.sanitize('display_name').trim();
        req.sanitize('display_name').escape();
        req.sanitize('email').trim();
        req.sanitize('email').escape();
        req.sanitize('description').trim();
        req.sanitize('description').escape();
        let user = new User();
        user.display_name = req.body.display_name;

        user.email = req.body.email;
        if (req.body.description) {
            user.description = req.body.description;
        }
        user.password_hash = req.body.password;

        user.save(function (err, userModel) {
            if (err) {
                res.status(501);
                res.send(err);
            } else {
                // passport.authenticate("jwt", {
                //     successRedirect: '/newUserSuccess',
                //     failureRedirect: '/',
                // });
                const token = generateToken(userModel);
                res.status(200);
                res.json({
                    "user": user.display_name,
                    "token": token
                });
            }
        });
    });
};