/**
 * Created by ace1_ on 16-Jun-17.
 */

import {NextFunction, Request, Response, Router} from "express";
import {Hashtag} from "../models/hashtags";

export class HashRouter {
    router: Router;

    /**
     * Initialize the HashRouter
     */
    constructor() {
        this.router = Router();
        this.init();
    }

    /**
     * GET all hashtags.
     */
    public getAll(req: Request, res: Response, next: NextFunction) {
        Hashtag.find({}, function (err, posts) {
            res.send(posts);
        });
    }

    /**
     * GET hashtag by name
     */
    public getOne(req: Request, res: Response, next: NextFunction) {
        let query = (req.params.name);
        let post = Hashtag.findOne({"name": query}, function (err, hashtag) {
            if (hashtag) {
                res.status(200)
                    .send({
                        message: 'Success',
                        status: res.status,
                        hashtag
                    });
            }
            else {
                res.status(404)
                    .send({
                        message: 'No hashtag found with the given name.',
                        status: res.status
                    })
            }
        });
    }

    public addOne(req: Request, res: Response, next: NextFunction) {
        Hashtag.update({name: req.body.name},
            {$inc: {["counter"]: 1}},
            {upsert: true},
            function (error, savedHashtagModel) {
                if (error) {
                    console.log("Error, Hashtag not saved");
                    next(error);
                    return;
                }
                console.log("Success: Hashtag successfully inserted/updated");
                return res.json(savedHashtagModel);
            });
    }

    public popular(req: Request, res: Response, next: NextFunction) {
        Hashtag.find({})
            .select("name")
            .sort({"counter": "-1"})
            .limit(5)
            .exec((err,data) => {
                if (err) {
                    res.status(500).send({message:"Could not load popular hashtags"});
                    return;
                }
                return res.send(data);
            });
    }
        /**
     * Take each handler, and attach to one of the Express.Router's
     * endpoints.
     */
    init() {
        this.router.get('/', this.getAll);
            this.router.get('/tags/:name', this.getOne);
        this.router.post('/', this.addOne);

            this.router.get("/popular/", this.popular);
    }

}

// Create the HashRouter, and export its configured Express.Router
const hashRoutes: HashRouter = new HashRouter();
hashRoutes.init();

let router = hashRoutes.router;
export default router;