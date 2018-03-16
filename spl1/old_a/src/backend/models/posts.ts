/**
 * Created by ace1_ on 16-Jun-17.
 */
import {Document, DocumentQuery, Model, model, Schema} from "mongoose";
import * as path from "path";
import {Hashtag, IHashtagModel} from "./hashtags";
import {User, IUserModel} from "./users"



export const export_path = path.join("posts");

export const upload_path = path.join(__dirname, "..", "static", "posts");


interface IStepPost {
    title: String,
    cover: String,
    description: String,
    materials: String[]
}

let StepSchema = new Schema({
    title: {type: String, default: "no title"},
    cover: {type: String, default: path.join(export_path, "no-image.jpg")},
    description: {type: String, default: "no description"},
    materials: [String]
});

interface IPost {
    authors: Schema.Types.ObjectId[],
    title: String,
    description: String,
    hashtags: Schema.Types.ObjectId[],
    comments: {
        body: String, date: Date,
        user: Schema.Types.ObjectId
    }[],
    upload_date: Date ,
    creation_date: Date,
    voters: Schema.Types.ObjectId[],
    steps: IStepPost[]
}
let PostSchema = new Schema({
    authors: [{type: Schema.Types.ObjectId, ref: "User"}],
    title: {type: String, required: true},
    description: {type: String, required: true},
    hashtags: [{type: Schema.Types.ObjectId, ref: "Hashtags"}],
    comments: [{body: {type: String, required: true}, date: Date, user: {type: Schema.Types.ObjectId, ref: "User"}}],
    upload_date: {type: Date, default: Date.now},
    creation_date: {type: Date, default: Date.now},
    voters: [{type: Schema.Types.ObjectId, ref: "User"}],
    steps: [StepSchema]
});

PostSchema.methods.addComment = function (username: String, comment: String, callback: any) {

};

PostSchema.pre("save", function (next) {

    // validation logic
    // no authors, no play
    if (this.authors.length == 0) {
        console.warn("invalid author length in ", this);
        next(new Error("Invalid author length!"));
    }

    let now = new Date();
    if (!this.upload_date) {
        this.upload_date = now;
    }
    if (!this.creation_date) {
        this.creation_date = now;
    }

    next();

});

interface IPostModel extends IPost, Document {
}
interface IStepModel extends IStepPost, Document {
}

export const Post: Model<IPostModel> = model<IPostModel>("Post", PostSchema);
export const StepPost: Model<IStepModel> = model<IStepModel>("Steps", StepSchema);


