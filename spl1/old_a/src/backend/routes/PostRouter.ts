/**
 * Created by ace1_ on 16-Jun-17.
 */

import {NextFunction, Request, Response, Router} from "express";
import {Schema} from "mongoose";

import * as multer from "multer";
import * as path from "path";
import * as passport from "passport";
import {export_path, Post, StepPost, upload_path} from "../models/posts";
import {Hashtag} from "../models/hashtags";
import {User} from "../models/users";

const allowed_ext: String[] = [".png", ".jpg", ".gif", '.jpeg'];


export class PostRouter {
    router: Router;
    upload: multer.Instance;

    /**
     * Initialize the PostRouter
     */
    constructor() {
        this.upload = multer({
            dest: upload_path, fileFilter: function (req, file, callback) {
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
     * GET all Posts.
     */
    public getAll(req: Request, res: Response, next: NextFunction) {
        Post.find({})
            .limit(5)
            .populate("authors hashtags")
            .populate("comments.user")
            .populate("voters")
            .sort({"upload_date": "-1"})
            .exec(function (err, posts) {
                if (err) {
                    res.status(500).send(err)
                } else {
                    res.send(posts);
                }
            });
    }


    /**
     * GET all posts by hash
     */
    public getOneByHash(req: Request, res: Response, next: NextFunction) {
        let query = req.params.hashtag;
        let hashtag_promise = new Promise((resolve, reject) => {
            Hashtag.findOne({name: query}, "_id", function (error, hashtag) {
                if (error) {
                    reject(error);
                } else {
                    resolve(hashtag._id);
                }
            })
        });
        hashtag_promise.catch((err) => res.status(500).send(err));
        hashtag_promise.then((id: Schema.Types.ObjectId) => Post.find({hashtags: id}).populate("authors hashtags")
            .populate("comments.user")
            .populate("voters")
            .sort({"upload_date": "-1"})
            .exec(function (err, posts) {
                if (err) {
                    res.status(500).json(err);
                    return;
                }
                if (posts) {
                    res.send(posts);
                } else {
                    res.status(404)
                        .send({
                            message: 'No post found with the given hashtag.',
                            status: res.status
                        });
                }
            }));


    }

    /**
     * GET all posts with user as author
     */
    public getOneByAuthor(req: Request, res: Response, next: NextFunction) {
        let author_name = req.params.author;
        let query = User.findOne({display_name: author_name}, "_id");
        query.then(function (author_id) {

            Post.find({authors: author_id}).populate("authors hashtags")
                .populate("comments.user")
                .populate("voters")
                .sort({"upload_date": "-1"})
                .exec(function (err, posts) {
                    if (err) {
                        res.status(500).json(err);
                        return;
                    }
                    if (posts) {
                        res.send(posts);
                    } else {
                        res.status(404)
                            .send({
                                message: 'No post found with the given user as author.',
                                status: res.status
                            });
                    }
                });
        });
    }


    public getPostSteps(req: Request, res: Response, next: NextFunction) {
        const index: number = parseInt(req.params.index);
        Post.findOne({_id: req.params.id}, function (err, post) {
            if (err) {
                res.status(500).json(err);
                return;
            }
            if (!post) {
                res.status(404)
                    .send({
                        message: 'No post found with the given user as author.',
                        status: res.status
                    });
            } else {
                if (index <= post.steps.length) {
                    let step = post.steps[index];
                    res.send(step);
                } else {
                    res.status(403)
                        .send({
                            message: 'Invalid step index',
                            status: res.status
                        });
                }
            }
        });
    }

    public addPostOverall(req: Request, res: Response, next: NextFunction) {
        let validationSchema = {
            'title': {
                notEmpty: true,
                errorMessage: 'Iinvalid title'
            },
            'description': {
                notEmpty: true,
                errorMessage: 'Invalid description' // Error message for the parameter
            },
            'authors': {
                notEmpty: true,
                errorMessage: 'Invalid authors' // Error message for the parameter
            },
        };
        req.check(validationSchema);


        req.getValidationResult().then(function (result) {
            if (!result.isEmpty()) {
                let errorMsg = JSON.stringify(result.mapped());
                res.status(400).send(errorMsg);
                return;
            }
            req.sanitize('authors').trim();
            req.sanitize('title').trim();
            req.sanitize('title').escape();
            req.sanitize('description').trim();
            req.sanitize('description').escape();
            let post = new Post();

            post.title = req.body.title;
            post.description = req.body.description;

            if (req.body.time) {
                post.creation_date = req.body.time;
            }

            let author_names: String[] = [];
            if (Array.isArray(req.body.authors)) {
                author_names = req.body.authors;
            } else {
                if (req.body.authors.length == 0) {
                    author_names = [req.user.display_name];
                } else {
                    author_names = req.body.authors.split(",");
                }
            }

            let hashtag_names: String[] = [];
            if (req.body.hashtags) {
                if (Array.isArray(req.body.hashtags)) {
                    hashtag_names = req.body.hashtags;
                } else {
                    hashtag_names = req.body.hashtags.split(",");
                }
            }

            /* convert the strings into IDs before saving */
            let author_ids: Schema.Types.ObjectId[] = [];
            let author_promise = new Promise((resolve, reject) => {
                User.find({display_name: {$in: author_names}}, "_id", function (error, users) {
                    if (error) {
                        reject(error);
                    } else {
                        resolve(users.map((user) => user._id));
                    }
                });
            });
            author_promise.catch((err) => res.status(500).send(err));
            author_promise.then((ids: Schema.Types.ObjectId[]) => {
                post.authors = ids
            });

            let hashtag_ids: Schema.Types.ObjectId[] = [];
            let hash_queries: Promise<any>[] = hashtag_names.map((tag) => {
                return new Promise((resolve, reject) => {
                    Hashtag.findOneAndUpdate({name: tag},
                        {$inc: {["counter"]: 1}},
                        {upsert: true, new: true},
                        function (error, savedHashtagModel) {
                            if (error) {
                                let errMsg = "Error: Hashtag " + tag + " not saved. Error " + error.toString();
                                console.log(errMsg);
                                reject(error);
                            }
                            console.log("Success: Hashtag " + savedHashtagModel + " successfully inserted/updated");
                            hashtag_ids.push(savedHashtagModel._id);
                            resolve(savedHashtagModel);
                        });
                })
            });
            Promise.all(hash_queries).then(() => post.hashtags = hashtag_ids);
            let final_queries = hash_queries.concat(author_promise);
            Promise.all(final_queries)
                .catch((err) => res.status(400).send(err))
                .then(() => {
                        if (post.authors.length != author_names.length) {
                            res.status(400).send({error: "Atleast one of the authors isn't a real user"});
                        } else {
                            post.save(function (err, userModel) {
                                if (err) {
                                    res.status(501);
                                    res.send(err);
                                } else {
                                    res.json({
                                        "post": post.id,
                                    });
                                }
                            })
                        }
                    }
                )
        });
    }

    /***
     * Add an image
     * @param {Request} req
     * @param {Response} res
     * @param {e.NextFunction} next
     */
    public addPostCover(req: Request, res: Response, next: NextFunction) {

        let path_name = path.join(export_path, req.file.filename);
        Post.findOneAndUpdate(
            {_id: req.params.id},
            //using deprecated syntax
            {$set: {"cover": path_name}},
            function (err, update) {
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

    public addSubPost(req: Request, res: Response, next: NextFunction) {
        let step = new StepPost();
        step.title = req.body.title;
        step.description = req.body.description;

        if (Array.isArray(req.body.materials)) {
            step.materials = req.body.materials;
        } else {
            step.materials = req.body.materials.split(",");
        }

        //step.save();

        Post.findOneAndUpdate({_id: req.params.id},
            {$push: {"steps": step}}, {new: true},
            function (err, post) {
                if (err) {
                    res.status(500).json(err);
                    return;
                }
                if (!post) {
                    res.status(404)
                        .send({
                            message: 'No post found with the given id.',
                            status: res.status
                        });
                } else {
                    res.status(200).json(post);
                }
            });
    }

    public addSubPostCover(req: Request, res: Response, next: NextFunction) {
        let path_name: string = path.join(export_path, req.file.filename);
        let index = parseInt(req.params.index);
        Post.findOne({_id: req.params.id}, function (err, post) {
            if (err) {
                res.status(500).json(err);
                return;
            }
            if (!post) {
                res.status(404)
                    .send({
                        message: 'No post found with the given id.',
                        status: res.status
                    });
            } else {
                if (index <= post.steps.length) {
                    post.steps[index].cover = path_name;
                    post.save((err, success) => {
                        if (err) {
                            res.status(500).json(err);
                        } else {
                            res.status(200)
                                .send({
                                    message: 'Success',
                                });
                        }
                    });
                } else {
                    res.status(403)
                        .send({
                            message: 'Invalid step index',
                            status: res.status
                        });
                }
            }
        });
    }

    public deleteById(req: Request, res: Response, next: NextFunction) {

        Post.findByIdAndRemove({_id: req.params.id}, function (err, post) {
            if (err) {
                res.status(500).json(err);
                return;
            }
            if (post) {
                res.status(200)
                    .send({
                        message: 'Success',
                    });
            } else {
                res.status(404).send({message: "Id Not found"});
            }
        });
    }

    public addPostComment(req: Request, res: Response, next: NextFunction) {
        let comment = req.body.comment;
        let userName = req.user.display_name;

        let promise = User.findOne({display_name: userName}, "_id").exec();
        promise.catch((err) => res.status(500).send(err));
        promise.then((user_id) => {
            let new_comment = {body: comment, date: new Date(), user: user_id};
            Post.findByIdAndUpdate({_id: req.params.id},
                {$push: {"comments": new_comment}},
                {new: true})
                .populate("voters")
                .populate("authors hashtags")
                .populate("comments.user")
                .exec(
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
                            res.status(500).send({message: "Can't find the post to add comment"});
                        }
                    }
                });
        });
    }

    public stream(req: Request, res: Response, next: NextFunction) {
        let userName = req.user.display_name;
        let follow_query = User.findOne({"display_name": userName}).populate("user_follow_list hashtag_follow_list")
            .exec();
        follow_query.catch((err) => res.status(500).send(err));
        follow_query.then((user) => {
            let post_query = Post.find({
                $or: [{hashtags: {$in: user.hashtag_follow_list}},
                    {authors: {$in: user.user_follow_list}}]
            })
                .sort({"upload_date": "-1"})
                .populate("authors hashtags")
                .populate("voters")
                .populate("comments.user").exec();
            post_query.catch((err) => res.status(500).send(err));
            post_query.then((result_posts) => res.send(result_posts));
        });
    }

    public addVote(req: Request, res: Response, next: NextFunction) {
        let userName = req.user.display_name;
        let promise = User.findOne({display_name: userName}, "_id").exec();
        promise.catch((err) => res.status(500).send(err));
        promise.then((user_obj) => {
            Post.findOneAndUpdate({_id: req.params.id, "voters": {"$ne": user_obj._id}},
                {$push: {"voters": user_obj._id}},
                {new: true})
                .populate("voters")
                .populate("authors hashtags")
                .populate("comments.user")
                .exec(
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
                            res.status(500).send({message: "Can't find the post to add voter. Or you have already voted"});
                        }
                    }
                });
        });
    }

    public deleteVote(req: Request, res: Response, next: NextFunction) {
        let userName = req.user.display_name;
        let promise = User.findOne({display_name: userName}, "_id").exec();
        promise.catch((err) => res.status(500).send(err));
        promise.then((user_obj) => {
            Post.findByIdAndUpdate({_id: req.params.id},
                {$pull: {voters: user_obj._id}},
                {new: true})
                .populate("voters")
                .populate("authors hashtags")
                .populate("comments.user")
                .exec(
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
                            res.status(500).send({message: "Can't find the post to delete voter"});
                        }
                    }
                });
        });
    }

    public search(req: Request, res: Response, next: NextFunction) {
        let search_body = req.body.search;

        let person_query = User.find({"display_name": search_body})
            .select("user_follow_list hashtag_follow_list")
            .populate("user_follow_list hashtag_follow_list")
            .exec();
        let hash_query = Hashtag.find({"name": search_body})
            .select("_id")
            .exec();

        person_query.catch((err) => res.status(500).send(err));
        hash_query.catch((err) => res.status(500).send(err));
        let user_ids = [];
        let hash_ids = [];
        person_query.then((ids_obj) => user_ids = ids_obj.map((obj)=>obj._id));
        hash_query.then((ids_obj) => hash_ids = ids_obj.map((obj)=>obj._id));
        let final_query = Promise.all([person_query, hash_query]).then(() => {
                let post_query = Post.find({
                    $or: [{hashtags: {$in: hash_ids}},
                        {authors: {$in: user_ids}}]
                })
                    .sort({"upload_date": "-1"})
                    .populate("authors hashtags")
                    .populate("voters")
                    .populate("comments.user").exec();
                post_query.catch((err) => res.status(500).send(err));
                post_query.then((result_posts) => res.send(result_posts));
            });
    }

    public stub(req: Request, res: Response, next: NextFunction) {
        res.send({
            message: "stub",
            req
        })
    }


    /**
     * Take each handler, and attach to one of the Express.Router's
     * endpoints.
     */
    init() {
        this.router.get('/', this.getAll);
        //get all posts with this hashtag
        this.router.get('/hashtag/:hashtag', this.getOneByHash);
        //get all posts by this user
        this.router.get('/name/:author', this.getOneByAuthor);

        this.router.get('/:id/:index', this.getPostSteps);

        this.router.post('/', passport.authenticate("jwt", {
            failureFlash: true
        }), this.addPostOverall);

        this.router.post('/:id/cover', passport.authenticate("jwt", {
            failureFlash: true
        }), this.upload.single("cover"), this.addPostCover);
        //add sub post
        this.router.post('/:id/subpost', passport.authenticate("jwt", {
            failureFlash: true
        }), this.addSubPost);
        this.router.post('/:id/:index/cover', passport.authenticate("jwt", {
            failureFlash: true
        }), this.upload.single("cover"), this.addSubPostCover);

        this.router.post("/:id/comment", passport.authenticate("jwt", {
            failureFlash: true
        }), this.addPostComment);


        this.router.post("/:id/vote", passport.authenticate("jwt", {
            failureFlash: true
        }), this.addVote);

        this.router.delete("/:id/vote", passport.authenticate("jwt", {
            failureFlash: true
        }), this.deleteVote);

        this.router.delete('/:id', passport.authenticate("jwt", {
            failureFlash: true
        }), this.deleteById);


        //get all posts this user name should see
        this.router.get('/stream/', passport.authenticate("jwt", {
            failureFlash: true
        }), this.stream);

        this.router.post("/search/", this.search);
    }

}

// Create the PostRouter, and export its configured Express.Router
const postRoutes: PostRouter = new PostRouter();
postRoutes.init();

let router = postRoutes.router;
export default router;