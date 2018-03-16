/**
 * Created by ace1_ on 16-Jun-17.
 */

import {NextFunction, Request, Response, Router} from "express";
import * as multer from "multer";
import * as passport from "passport";
import * as path from "path";
import {avatar_path, export_path, User} from "../models/users";
import {isRightUser} from "./authentication";


const allowed_ext: String[] = [".png", ".jpg", ".gif", '.jpeg'];

export class ProfileRouter {
    router: Router;
    upload: multer.Instance;

    /**
     * Initialize the HashRouter
     */
    constructor() {
        this.upload = multer({
            dest: avatar_path, fileFilter: function (req, file, callback) {
                const ext = path.extname(file.originalname);
                if (-1 == allowed_ext.indexOf(ext)) {
                    let errMsg = 'Only images are allowed';
                    let err: Error = new Error(errMsg);
                    return callback(err, false);
                }
                callback(null, true)
            }
        });
        this.router = Router();
        this.init();
    }

    /**
     * GET all users.
     */
    public getAll(req: Request, res: Response, next: NextFunction) {
        User.find({}).populate("user_follow_list hashtag_follow_list")
            .exec(function (err, users) {
                res.send(users);
            });
    }

    /**
     * GET users by name
     */
    public getOne(req: Request, res: Response, next: NextFunction) {
        let query = (req.params.display_name);
        let post = User.findOne({"display_name": query}).populate("user_follow_list hashtag_follow_list")
            .exec(function (err, user) {
                if (user) {
                    res.status(200)
                        .send({
                            message: 'Success',
                            status: res.status,
                            user
                        });
                }
                else {
                    res.status(404)
                        .send({
                            message: 'No user found with the given name.',
                            status: res.status
                        })
                }
            });
    }

    /**
     * GET all the users this user is following.
     */
    public getUserFollowingList(req: Request, res: Response, next: NextFunction) {
        let query = (req.params.display_name);
        let post = User.findOne({"display_name": query}, "user_follow_list").populate("user_follow_list")
            .exec(function (err, user) {
                if (user) {
                    res.status(200)
                        .send({
                            message: 'Success',
                            status: res.status,
                            user
                        });
                }
                else {
                    res.status(404)
                        .send({
                            message: 'No user found with the given name.',
                            status: res.status
                        })
                }
            });
    }

    /**
     * POST a new to_follow for User
     */
    public addFollowing(req: Request, res: Response, next: NextFunction) {
        let query = (req.params.display_name);
        let to_follow = (req.params.to_follow);
        if (query === to_follow) {
            res.status(400).json({error: "Can't follow self..."});
            return;
        }
        User.findOne({"display_name": to_follow}, "_id", function (err, user) {
            if (err) {
                res.status(500);
                res.json(err);
                return;
            }
            let user_id = user._id;
            User.findOneAndUpdate(
                {"display_name": query, "user_follow_list": {"$ne": user_id}},
                {$push: {"user_follow_list": user_id}},
                {new: true},
                function (err, raw) {
                    if (err) {
                        res.status(500).json(err);
                    } else {
                        if (raw) {
                            res.status(200)
                                .send({
                                    message: 'Success',
                                    raw
                                });
                        } else {
                            res.status(500).send({message: "Can't find anyone or user is already being followed"});
                        }
                    }
                });
        });
    }

    /**
     * Delete a user from a list of following.
     */
    public deleteFollowing(req: Request, res: Response, next: NextFunction) {
        let querying_user = (req.params.display_name);
        let to_delete = (req.params.to_delete);
        User.findOne({"display_name": to_delete}, "_id", function (err, user) {
            if (err) {
                res.status(500);
                res.json(err);
                return;
            }
            let user_id = user._id;

            User.findOneAndUpdate(
                {display_name: querying_user},
                {$pull: {user_follow_list: user_id}},
                {new: true},
                function (err, raw) {
                    if (err) {
                        res.status(500).json(err);
                    } else {
                        if (raw) {
                            res.status(200)
                                .send({
                                    message: 'Success',
                                    raw
                                });
                        } else {
                            res.status(500).send({message: "Can't find anyone"});
                        }
                    }
                });
        });
    }

    public getAllFollowingUser(req: Request, res: Response, next: NextFunction) {
        let user_being_queried = (req.params.display_name);
        User.findOne({"display_name": user_being_queried}, "_id", function (err, user) {
            if (err) {
                res.status(500);
                res.json(err);
                return;
            }
            let user_id = user._id;
            User.find({user_follow_list: user_id}, "display_name", function (err, docs) {
                if (err) {
                    res.status(500).json(err);
                } else {
                    res.status(200)
                        .send({
                            message: 'Success',
                            docs
                        });
                }
            });
        });
    }

    /***
     * Update a users fields except password
     */
    public updateUser(req: Request, res: Response, next: NextFunction) {
        const querying_user = (req.params.display_name);
        const payload = req.body;

        let validationSchema = {
            'email': {
                notEmpty: true,
                isEmail: {
                    errorMessage: 'Invalid Email'
                }
            }
        };
        req.check(validationSchema);
        req.getValidationResult().then(function (result) {
            if (!result.isEmpty()) {
                let errorMsg = JSON.stringify(result.mapped());
                res.status(400).send(errorMsg);
                return;
            }
            req.sanitize('email').trim();
            req.sanitize('email').escape();
            req.sanitize('description').trim();
            req.sanitize('description').escape();

            interface updateFields {
                description?: String,
                email?: String
            }

            let objForUpdate: updateFields = {};

            //lets collect new fields
            if (payload.description) {
                objForUpdate.description = payload.description;
            }
            if (payload.email) {
                objForUpdate.email = payload.email;
            }
            User.findOneAndUpdate(
                {display_name: querying_user},
                //using deprecated syntax
                {
                    $set: objForUpdate
                }, function (err, update) {
                    if (err) {
                        res.status(500).json(err);
                    } else {
                        res.status(200)
                            .send({
                                message: 'Success',
                            });
                    }
                });
        });
    }

    /***
     * Add an image to the given profile
     * @param {Request} req
     * @param {Response} res
     * @param {e.NextFunction} next
     */
    public addImage(req: Request, res: Response, next: NextFunction) {
        const querying_user = (req.params.display_name);
        let avatar_file = path.join(export_path, req.file.filename);
        User.findOneAndUpdate(
            {display_name: querying_user},
            //using deprecated syntax
            {
                $set: {avatar: avatar_file}
            }, function (err, update) {
                if (err) {
                    res.status(500).json(err);
                } else {
                    res.status(200)
                        .send({
                            message: 'Success',
                        });
                }
            });
    }

    /**
     * Take each handler, and attach to one of the Express.Router's
     * endpoints.
     */
    init() {
        this.router.get('/', this.getAll);
        this.router.get('/:display_name', this.getOne);


        this.router.get('/:display_name/following', passport.authenticate("jwt", {
            failureFlash: true
        }), this.getUserFollowingList);
        this.router.post("/:display_name/follow/:to_follow", passport.authenticate("jwt", {
            failureFlash: true
        }), isRightUser, this.addFollowing);

        this.router.get('/:display_name/followers', passport.authenticate("jwt", {
            failureFlash: true
        }), this.getAllFollowingUser);

        this.router.delete("/:display_name/follow/:to_delete", passport.authenticate("jwt", {
            failureFlash: true
        }), isRightUser, this.deleteFollowing);
        //update a profile
        //need to authenticate
        this.router.put("/:display_name", passport.authenticate("jwt", {
            failureFlash: true
        }), isRightUser, this.updateUser);

        this.router.put("/:display_name/image", passport.authenticate("jwt", {
            failureFlash: true
        }), isRightUser, this.upload.single("avatar"), this.addImage);
    }

}

// Create the HashRouter, and export its configured Express.Router
const profileRoutes: ProfileRouter = new ProfileRouter();
profileRoutes.init();

let router = profileRoutes.router;
export default router;