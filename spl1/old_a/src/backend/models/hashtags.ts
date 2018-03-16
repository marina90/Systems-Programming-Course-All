/**
 * Created by acepace on 17/06/2017.
 */
import {Document, Model, model, Schema} from "mongoose";

interface IHashtag {
    name: String,
    counter: Number
}

let HashSchema = new Schema({
    name: {type: String, minlength: [3, 'The hashtag name must be at least ({MINLENGTH}) characters']},
    counter: {type: Number, default: 0}
});

HashSchema.pre("save", function (next) {
    if (!this.counter) {
        this.counter = 0;
    }
    next();
});

export interface IHashtagModel extends IHashtag, Document {
}

export const Hashtag: Model<IHashtagModel> = model<IHashtagModel>("Hashtags", HashSchema);

