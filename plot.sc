if(list_files('', 'json')~'plots' == null,
    write_file('plots', 'json', {'plots' -> {}})
);

global_regions = {};
plots = read_file('plots', 'json');
for(plots:'plots',
    plotname = _;
    for(plots:'plots':plotname:'positions', global_regions = global_regions + {_ -> plotname})
);

__config()->
{
    'commands' ->
    {
        '' -> _() -> print(player(), ''),
        'claim <name>' -> _(name) -> plot_claim(name, 'lime', false),
        'claim <name> <color>' -> _(name, color) -> plot_claim(name, color, false),
        'claim <name> <color> overwrite' -> _(name, color) -> plot_claim(name, color, true),
        'unclaim' -> _() -> plot_unclaim(false),
        'unclaim confirm' -> _() -> plot_unclaim(true),
        'modify <name_existing> tp <dimension> <location> <rotation>' -> _(name, dimension, location, rotation) -> plot_modify(name, 'tp', [dimension, location:0, location:1, location:2, rotation:1, rotation:2]),
        'modify <name_existing> name <name>' -> _(name, newName) -> plot_modify(name, 'name', newName),
        'tp <name_existing>' -> 'plot_tp',
        'query' -> _() -> plot_query(player()~'pos'),
        'join <name_existing>' -> 'plot_join',
        'delete <name_existing>' -> 'plot_delete',
        'list' -> 'plot_list'
    },
    'arguments' ->
    {
        'name' -> {'type' -> 'string', 'suggest' -> []},
        'name_existing' -> {'type' -> 'string', 'suggester' -> _(args) ->
        (
            plots = read_file('plots', 'json');
            keys(plots:'plots')
        )},
        'newName' -> {'type' -> 'string'},
        'color' -> {'type' -> 'string', 'options' -> ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black', 'none']}
    },
    'scope' -> 'global'
};

plot_claim(name, color, overwrite) ->
(
    player = player();
    pos = player~'pos' / 512;
    pos = [player~'dimension', ceil(pos:0) - 1, ceil(pos:2) - 1];

    plots = read_file('plots', 'json');

    plotname = global_regions:pos;
    if(plotname != null,
        if(plotname == name,
            print(player, format('l This region is already part of ', 't ' + name));
            return()
        );
        plot = plots:'plots':plotname;
        if(overwrite,
            if(length(plot:'positions') > 1,
                delete(plots:'plots':plotname:'positions', plot:'positions'~pos);
                delete(global_regions, pos)
            ,
                print(player, format('l Overwriting this region will delete plot ', 't ' + plotname, 'l . If you want to continue, run ', 'm [/plot delete ' + plotname + ']', '!/plot delete ' + plotname, 'l  and then you can claim this region'));
                return()
            );
            if(!add_region_to_plot(plots, pos, name, color, player),
                new_plot_entry(plots, pos, name, player, color, player)
            )
        ,    
            if(length(plot:'positions') == 1,
                print(player, format('l Overwriting this region will delete plot ', 't ' + plotname, 'l . If you want to continue, run ', 'm [/plot delete ' + plotname + ']', '!/plot delete ' + plotname, 'l  and then you can claim this region')),
            print(player, format('l Region already taken by plot ', 't ' + plotname, 'l . Run ', 'm [/plot claim ' + name + ' <color> overwrite]', '!/plot claim ' + name + ' ' + color + ' overwrite', 'l  to overwrite it'))
            )
        );
        return()
    );

    if(!add_region_to_plot(plots, pos, name, color, player),
        new_plot_entry(plots, pos, name, player, color, player)
    )
);

add_region_to_plot(plots, pos, name, color, player) ->
(
    plot = plots:'plots':name;
    if(plot != null,
        plots:'plots':name:'positions':length(plot:'positions') = pos;
        global_regions = global_regions + {pos -> name};
        print(player, format('r Region ', 't ' + pos, 'r  added to plot ', 't ' + name));
        highlight_region(pos, color);
        write_file('plots', 'json', plots);
        return(true)
    );
    return(false)
);

new_plot_entry(plots, pos, name, owners, color, player) ->
(
    plots:'plots' = plots:'plots' + {name -> {'owners' -> owners, 'positions' -> [pos], 'tp' -> [pos:0, pos:1 * 512 + 256, 1, pos:2 * 512 + 256, 180, 0], 'subplots' -> []}}; 
    global_regions = global_regions + {pos -> name};
    print(player, format('r Region ', 't ' + pos, 'r  claimed for new plot ', 't ' + name, 'r  by ', 't ' + owners));
    highlight_region(pos, color);
    write_file('plots', 'json', plots)
);

plot_unclaim(confirm) ->
(
    plots = read_file('plots', 'json');
    player = player();
    pos = player~'pos' / 512;
    pos = [player~'dimension', ceil(pos:0) - 1, ceil(pos:2) - 1];

    plotname = global_regions:pos;
    if(plotname != null,
        plot = plots:'plots':plotname;
        if(length(plot:'positions') == 1,
                print(player, format('l Unclaiming this region will delete plot ', 't ' + plotname, 'l . If you want to continue, run ', 'm [/plot delete ' + plotname + ']', '!/plot delete ' + plotname));
                return()
        );
        if(confirm,
            delete(plots:'plots':plotname:'positions', plot:'positions'~pos);
            delete(global_regions, pos);
            write_file('plots', 'json', plots);
            print(player, format('r Unclaiming region ', 't ' + pos, 'r  from ', 't ' + plotname));
            unhighlight_region(pos);
            return()
        ,
            print(player, format('r Current region is part of ', 't ' + plotname, 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'),
            'r  regions total. Click ', 'm HERE', '!/plot unclaim confirm', 'r  to confirm'));
        );
        return()
    );
    print(player, format('l Current region is unclaimed'))
);

plot_query(pos) ->
(
    plots = read_file('plots', 'json');
    player = player();
    pos = [player~'dimension', ceil(pos:0 / 512) - 1, ceil(pos:2 / 512) - 1];

    plotname = global_regions~pos;
    if(plotname != null,
        plot = plots:plotname;
        print(player, format('r Current region is part of ', 't ' + plotname, 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'), 'r  regions total'));
        return()
    );
    print(player, format('r Current region is unclaimed'))
);

plot_join(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    plot = plots:'plots':name;
    if(plot != null,
        if(plot:'owners'~player != null,
            print(player, format('t ' + player, 'l  already owns plot ', 't ' + name));
            return();
        );
        plots:name:'owners':length(plot:'owners') = player();
        write_file('plots', 'json', plots);
        print(player, format('t ' + player, 'r  added to plot ', 't ' + name));
    );
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_delete(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    positions = plots:'plots':name:'positions';
    if(delete(plots:'plots', name),
        write_file('plots', 'json', plots);
        print(player, format('r ' + 'plot ', 't ' + name, 'r  deleted'));
        for(positions,
            unhighlight_region(_);
            delete(global_regions, _)
        );
        return()
    );
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_list() ->
(
    plots = read_file('plots', 'json');
    player = player();

    l = length(plots:'plots');
    if(l == 0,
        print(player, format('l No plots configured'));
        return()
    );

    print(player, '');
    print(player, format('mb List of plots'));
    for(plots:'plots',
        plot = plots:'plots':_;
        tp = plot:'tp';
        if(isInt(tp:1), tp:1 += '.0');
        if(isInt(tp:3), tp:3 += '.0');
        print(player, format('r - ', 'm [' + _ + ']', '^ TP', '!/plot tp ' + _, 'r  owned by ', 't ' + plot:'owners', 'r  with regions ') + plot_tp_list(_))
    )
);

plot_tp_list(name) ->
(
    plots = read_file('plots', 'json');

    plot = plots:'plots':name;
    string = '';
    for(plot:'positions',
        if(i > 0, string = string + format('r , '));
        dim = 't ';
        if(_:0 == 'the_nether', dim = 'n ');
        if(_:0 == 'the_end', dim = 'd ');
        string = string + format(dim + [_:1, _:2], '^ TP', '!' + teleport_command('@s', [_:0, _:1 * 512 + 256 + '.0', 1, _:2 * 512 + 256 + '.0', 180, 0]));
        i = 1
    );
    return(string)
);

plot_tp(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    plot = plots:'plots':name;
    if(plot != null,
        tp = plot:'tp';
        if(isInt(tp:1), tp:1 += '.0');
        if(isInt(tp:3), tp:3 += '.0');
        run(teleport_command('@s', tp));
        return()
    );
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_modify(name, property, value) ->
(
    plots = read_file('plots', 'json');
    player = player();

    plot = plots:'plots':name;
    if(plot != null,
        if(property == 'tp',
            if(plot:'positions'~[value:0, ceil(value:1 / 512) - 1, ceil(value:3 / 512) - 1] != null,
                plots:'plots':name:'tp' = value;
                write_file('plots', 'json', plots);
                print(player, format('r Teleport location for ', 't ' + name, 'r  set to ', 't ' + value));
                return()
            );
            print(player, format('l Teleport location is outside claimed area for ', 't ' + name));
            return()
        );
        if(property == 'name',
            if(plots:'plots':value != null,
                print(player, format('l Name ', 't ' + value, 'l  is already taken'));
                return()
            );
            plots:'plots':value = plot;
            delete(plots:'plots', name);
            write_file('plots', 'json', plots);
            for(plot:'positions', global_regions:_ = value);
            print(player, format('l Plot name changed from ', 't ' + name, 'l  to ', 't ' + value));
            return()
        )
    );
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

vanilla_fill(pos1, pos2, block) ->
(
    run('fill ' + pos1:0 + ' ' + pos1:1 + ' ' + pos1:2 + ' ' + pos2:0 + ' ' + pos2:1 + ' ' + pos2:2 + ' ' + block)
);

setblock(pos, block) ->
(
    run('setblock ' + pos:0 + ' ' + pos:1 + ' ' + pos:2 + ' ' + block)
);

teleport_command(e, pos) ->
(
    return('/execute in ' + pos:0 + ' run tp ' + e + ' ' + pos:1 + ' ' + pos:2 + ' ' + pos:3 + ' ' + pos:4 + ' ' + pos:5)
);

isInt(n) -> return(n - floor(n) == 0);

highlight_region(pos, color) ->
(
    if(color == 'none', return());
    in_dimension(pos:0,
        pos = [pos:1 * 512, 0, pos:2 * 512];
        block = color + '_stained_glass';

        if(pos:0 >= 0,
            add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
            schedule(1, _(outer(pos), outer(block)) ->
            (
                vanilla_fill(pos, pos + [511, 0, 0], block);
                vanilla_fill(pos + [511, 0, 0], pos + [511, 0, 511], block);
                vanilla_fill(pos + [511, 0, 511], pos + [0, 0, 511], block);
                vanilla_fill(pos + [0, 0, 511], pos, block);
            ));
            return()
        );

        if(pos:2 >= 0,
            add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);

            schedule(1, _(outer(pos), outer(block)) ->
                c_for(i = 0, i <= 496, i += 16,
                    setblock(pos + [i, 0, 0], block);
                    setblock(pos + [i + 15, 0, 0], block);

                    setblock(pos + [0, 0, i], block);
                    setblock(pos + [0, 0, i + 15], block);

                    setblock(pos + [i, 0, 511], block);
                    setblock(pos + [i + 15, 0, 511], block);

                    setblock(pos + [511, 0, i], block);
                    setblock(pos + [511, 0, i + 15], block);
                )
            )
        )
    )
);

unhighlight_region(pos) ->
(
    if(pos:1 >= 0, highlight_region(pos, 'blue'); return());

    if(pos:2 >= 0, in_dimension(pos:0,
        pos = [pos:1 * 512, 0, pos:2 * 512];

        add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
        schedule(1, _(outer(pos), outer(block)) ->
            c_for(i = 0, i <= 496, i += 16,
                setblock(pos + [i, 0, 0], 'blue_stained_glass');
                setblock(pos + [i + 15, 0, 0], 'blue_stained_glass');

                setblock(pos + [0, 0, i], 'blue_stained_glass');
                setblock(pos + [0, 0, i + 15], 'blue_stained_glass');

                setblock(pos + [i, 0, 511], 'blue_stained_glass');
                setblock(pos + [i + 15, 0, 511], 'blue_stained_glass');

                setblock(pos + [511, 0, i], 'blue_stained_glass');
                setblock(pos + [511, 0, i + 15], 'blue_stained_glass');
            )
        )
    ))
);

global_previous_plot = {};
__on_tick() ->
(
    plots = read_file('plots', 'json');
    for(player('all'), entity_event(_, 'on_move', _(e, m, p1, p2, outer(plots)) ->
    (
        if(ceil(p1:0 / 512) != ceil(p2:0 / 512) || ceil(p1:2 / 512) != ceil(p2:2 / 512),
            plotname = global_regions:[e~'dimension', ceil(p2:0 / 512) - 1, ceil(p2:2 / 512) - 1];
            if(plotname != global_previous_plot:e,
                if(plotname != null,
                    display_title(e, 'title', format('w Entering plot ', 't ' + plotname), 5, 15, 5);
                    global_previous_plot = global_previous_plot + {e -> plotname};
                    return()
                );
                global_previous_plot = global_previous_plot + {e -> null};
                display_title(e, 'title', format('w Exiting claimed area'))
            )
        )
    )))
)