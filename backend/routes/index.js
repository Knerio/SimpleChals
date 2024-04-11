const fetch = require("node-fetch");

const express = require('express');
const router = express.Router();
const {Readable} = require("stream")
const dotenv = require('dotenv');

dotenv.config();

router.get("/wrapper/latest", async (req, res) => {
    const packagesURL = `${process.env.BASE_GITHUB_URL}/${process.env.GITHUB_USERNAME}/packages/maven/${process.env.API_PACKAGE_PATH}.${process.env.API_NAME}/versions`;

    await downloadLatestJar(packagesURL, process.env.API_NAME, process.env.API_PACKAGE_PATH, res);
})

router.get('/latest/:name', async (req, res) => {
    const name = req.params.name;

    const packagesURL = `${process.env.BASE_GITHUB_URL}/${process.env.GITHUB_USERNAME}/packages/maven/${process.env.MODS_PACKAGE_PREFIX}.${name}/versions`;

    await downloadLatestJar(packagesURL, name, process.env.MODS_PACKAGE_PREFIX, res);

});

async function downloadLatestJar(packagesURL, name, path, res) {
    const response = await fetch(packagesURL, {
        headers: {
            'Authorization': 'Bearer ' + process.env.GITHUB_PASSWORD,
        }
    });


    if (!response.ok) {
        throw new Error(`Failed to fetch packages ${JSON.stringify(await response.json())}`);
    }

    const data = await response.json();
    const latestPackage = data.reduce((acc, item) => {
        const updatedItem = new Date(item["updated_at"]).getTime();
        const updatedAcc = new Date(acc["updated_at"]).getTime();
        if (updatedAcc > updatedItem) return acc;
        return item;
    }, {"updated_at": 0});

    console.log(latestPackage.name)
    const latestJarURL = `${process.env.BASE_MAVEN_URL}/${process.env.GITHUB_USERNAME}/${process.env.GITHUB_PROJECT_NAME}/${path}.${name}/${latestPackage.name}/${name}-${latestPackage.name}.jar`

    const jarResponse = await fetch(latestJarURL, {
        method: 'GET',
        responseType: 'blob',
        headers: {
            "Authorization": `Bearer ${process.env.GITHUB_PASSWORD}`
        }
    })


    res.setHeader('Content-Disposition', `attachment; filename="${name}.jar"`);
    res.setHeader('Content-Type', 'application/java-archive');
    Readable.fromWeb(jarResponse.body).pipe(res);
}

router.get('/mods', async (req, res) => {
    try {

        const response = await fetch(`${process.env.BASE_GITHUB_URL}/${process.env.GITHUB_USERNAME}/packages?package_type=maven`, {
            headers: {
                'Authorization': 'Bearer ' + process.env.GITHUB_PASSWORD,
            }
        });


        if (!response.ok) {
            throw new Error('Failed to fetch packages');
        }

        const data = await response.json();
        const jsonResponse = [];


        for (let i = 0; i < data.length; i++) {
            const name = data[i].name;
            if (!name.startsWith(`${process.env.MODS_PACKAGE_PREFIX}.`)) continue;
            jsonResponse.push(name.replace(`${process.env.MODS_PACKAGE_PREFIX}.`, ""))
        }

        res.json(jsonResponse);
    } catch (error) {
        console.error('Error fetching packages:', error);
        res.status(500).json({error: 'Failed to fetch packages'});
    }
});


module.exports = router;
