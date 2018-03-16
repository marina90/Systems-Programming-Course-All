/**
 * Created by ace1_ on 16-Jun-17.
 */
import {Document, Model, model, Schema} from "mongoose";
import * as bcrypt from "bcrypt-nodejs";
import * as path from "path";


export const export_path = path.join("profiles");
export const avatar_path = path.join(__dirname, "..", "static", "profiles");

interface IUser {
    display_name: {
        type: String,
        unique: true,
        required: true
    },
    description: String,
    avatar: String,
    user_follow_list: Schema.Types.ObjectId[],
    hashtag_follow_list: Schema.Types.ObjectId[],
    creation_date: Date,
    email: {
        type: String,
        unique: true,
        required: true
    },
    password_hash: String,
}

export let UserSchema = new Schema({
    display_name: {
        type: String,
        required: true
    },
    description: String,
    avatar: {type: String, default: path.join(export_path, "profile.png")},
    user_follow_list: [{type: Schema.Types.ObjectId, ref: "User"}],
    hashtag_follow_list: [{type: Schema.Types.ObjectId, ref: "Hashtags"}],
    creation_date: {type: Date, default: Date.now},
    email: {
        type: String,
        unique: true,
        required: true
    },
    password_hash: {type: String, select: false},
});


UserSchema.pre("save", function (next) {
    if (!this.isModified('password_hash')) return next();
    let user = this;

    bcrypt.genSalt(5, function (err, salt) {
        user.salt = salt;
        if (err) return next(err);

        bcrypt.hash(user.password_hash, salt, null, function (err, hash) {
            if (err) return next(err);
            user.password_hash = hash;
            next();
        });
    });
});

// Method to compare password for login
UserSchema.methods.comparePassword = function (userObject, candidatePassword, callback) {
    bcrypt.compare(candidatePassword, userObject.password_hash, function (err, isMatch) {
        if (err) {
            return callback(err);
        }

        callback(null, isMatch);
    });
};

export interface IUserModel extends IUser, Document {
    comparePassword(UserObject: IUserModel, passport: string, callback: any): void
}

export const User: Model<IUserModel> = model<IUserModel>("User", UserSchema);


