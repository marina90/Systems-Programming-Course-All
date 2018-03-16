/**
 * Created by acepace on 22/06/2017.
 */
import * as passport from "passport";
import {IUserModel, User} from "../models/users";
import {ExtractJwt, Strategy as JwtStrategy} from "passport-jwt";
import {sign as jwtSign} from "jsonwebtoken";

export const SECRET = "SECRETSECRET";

passport.serializeUser(function (user: IUserModel, done) {
    done(null, user.display_name);
});

passport.deserializeUser(function (display_name, done) {
    User.findOne({display_name: display_name}, function (err, user) {
        done(err, user);
    });
});


export const generateToken = function (userModel: IUserModel): String {
    let payload = {display_name: userModel.display_name};
    return jwtSign(payload, SECRET, {
        expiresIn: 10080 // in seconds
    });
};

export const jwtOptions = {
    // Telling Passport to check authorization headers for JWT
    jwtFromRequest: ExtractJwt.fromAuthHeader(),
    // Telling Passport where to find the secret
    secretOrKey: SECRET,
    passReqToCallback: true,
    ignoreExpiration: true, //hope for the best, die like the rest
};

// Setting up JWT login strategy
const jwtLogin = new JwtStrategy(jwtOptions, function (req, payload, done) {
    User.findOne({"display_name": payload.display_name}, function (err, user) {
        if (err) {
            return done(err);
        }
        let errorMsg = {message: 'Your login details could not be verified. Please try again.'};

        if (user) { //success
            return done(null, user);

        }
        return done(null, false, JSON.stringify(errorMsg));
    });
});

passport.use("jwt", jwtLogin);

